package com.example.TeamApp.event

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.TeamApp.data.Coordinates
import com.example.TeamApp.data.Event
import com.example.TeamApp.data.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.poi.StandardCategoryId.Companion.Locale
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.search.autocomplete.AutocompleteOptions
import com.tomtom.sdk.search.online.OnlineSearch
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
class CreateEventViewModel : ViewModel() {
    private val _sport = MutableLiveData<String>()
    val activityList =   mutableStateListOf<Event>()
    val sport: LiveData<String> get()= _sport
    var isDataFetched = false
    var newlyCreatedEvent by mutableStateOf<Event?>(null)
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    fun loadStuff(){
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000)
            _isLoading.value = false

        }
    }

    private var _mapFragment: MutableState<MapFragment?> = mutableStateOf(null)
    val mapFragment: MapFragment?
        get() = _mapFragment.value
    fun setMapFragment(fragment: MapFragment) {
        _mapFragment.value = fragment
    }
    var isMapInitialized = false
    fun initializeMapIfNeeded(context: Context) {
        Log.d("CreateEventViewModel", "initializeMapIfNeeded: $isMapInitialized")
        if (!isMapInitialized) {
            isMapInitialized = true
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val mapOptions = MapOptions(
                mapKey = getApiKey(context),
                // add more options
            )

            createTomTomMapFragment(fragmentManager, mapOptions) { fragment ->
                Log.d("CreateEventViewModel", "Map fragment created")
                setMapFragment(fragment)
            }
        }
    }

    private val _locationID = MutableLiveData<Map<String, Coordinates>>()
    val locationID: LiveData<Map<String, Coordinates>> get() = _locationID

    fun setLocationID(newLocationID: Map<String, Coordinates>) {
        _locationID.value = newLocationID
    }
    private val _dateTime = MutableLiveData<String>()
    val dateTime: LiveData<String> = _dateTime
    private val _description = MutableLiveData<String>()
    val description: LiveData<String> get() = _description
    private val _limit = MutableLiveData<String>()
    val limit: LiveData<String> get() = _limit
    fun navigateToProfile(navController: NavController){
        navController.navigate("profile"){
            popUpTo("createEvent"){inclusive = true}
        }
    }
    fun navigateToSearchThrough(navController: NavController){
        navController.navigate("search"){
            popUpTo("createEvent"){inclusive = true}
        }
    }
    fun createEvent(event: Event, userID:String, callback: (String?) -> Unit) {
        val db = Firebase.firestore
        Log.d("CreateEventViewModel", "createEvent: $event")
        Log.d("CreateEventViewModel", "userID: $userID")
        val userRef = db.collection("users").document(userID)
        val eventRef = db.collection("events").document()
        val eventWithId = event.copy(id = eventRef.id)
        eventRef.set(eventWithId)
            .addOnSuccessListener {
                Log.d("CreateEventViewModel", "Event successfully created with ID: ${eventRef.id}")
                callback(null)
            }
            .addOnFailureListener { e ->
                Log.w("CreateEventViewModel", "Error creating event", e)
                callback("Błąd, nie dodano Eventu")
            }
        userRef.update("attendedEvents", FieldValue.arrayUnion(eventRef.id))
            .addOnSuccessListener {
                Log.d("CreateEventViewModel", "Event ID successfully added to user's attendedEvents")
            }
            .addOnFailureListener { e ->
                Log.w("CreateEventViewModel", "Error updating attendedEvents", e)
            }


        activityList.add(0, eventWithId)
        newlyCreatedEvent = eventWithId
    }
    fun clearNewlyCreatedEvent(){
        newlyCreatedEvent = null
    }
    fun fetchEvents() {
        Log.d("CreateEventViewModel", "fetchEvents: $isDataFetched")
        if (isDataFetched) return
        Log.d("CreateEventViewModel", "fetchEvents")
        val db = Firebase.firestore
        db.collection("events").get()
            .addOnSuccessListener { result ->
                activityList.clear()
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    activityList.add(event)
                }
                if (!activityList.isEmpty()) {
                    Log.d("CreateEventViewModel", "events found")
                }
                Log.d("CreateEventViewModel", "Events fetched successfully")
                isDataFetched = true
            }
            .addOnFailureListener { e ->
                Log.w("CreateEventViewModel", "Error fetching events", e)
            }

    }

    fun fetchFilteredEvents(selectedSports: List<String>) {
        Log.d("CreateEventViewModel", "fetchFilteredEvents: Fetching events with selected sports")

        if (selectedSports.isEmpty()) {
            Log.d("CreateEventViewModel", "Selected sports list is empty, fetching all events.")
            fetchEvents() // Fetch all events or handle the empty case as needed
            return
        }

        val db = Firebase.firestore
        db.collection("events")
            .whereIn("activityName", selectedSports) // Assuming "sport" is a field in your Firestore event documents
            .get()
            .addOnSuccessListener { result ->
                activityList.clear()
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    activityList.add(event)
                }
                if (activityList.isNotEmpty()) {
                    Log.d("CreateEventViewModel", "Filtered events found")
                } else {
                    Log.d("CreateEventViewModel", "No events matched the selected sports")
                }
            }
            .addOnFailureListener { e ->
                Log.w("CreateEventViewModel", "Error fetching filtered events", e)
            }
    }

    fun getEventById(id: String): Event? {
        return activityList.find { it.id == id }
    }
    fun resetFields() {
        _sport.value = ""
        _location.value = ""
        _limit.value = ""
        _description.value = ""
        _dateTime.value = ""
    }
    //temporary here
    fun logout(navController: NavController) {
        FirebaseAuth.getInstance().signOut()
        navController.navigate("register") {
            popUpTo("createEvent") { inclusive = true }
        }
    }
    fun onSportChange(newSport: String) {
        _sport.value = newSport.toString()
    }
    fun onAddressChange(newAddress: String) {
        _location.value = newAddress
    }
    fun onDateChange(newDate: String) {
        _dateTime.value = newDate
    }
    fun onLimitChange(newLimit: String) {
        _limit.value = newLimit
    }
    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }
    fun getAvailableSports():List<String>{
        return Event.sportIcons.keys.toList()
    }



}