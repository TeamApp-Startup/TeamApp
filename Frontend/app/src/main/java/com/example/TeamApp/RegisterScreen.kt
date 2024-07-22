package com.example.TeamApp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.outlineLight
import com.example.compose.primaryLight
import com.example.compose.secondaryLight

@Composable
fun RegisterScreen() {
    Surface( modifier = Modifier
        .fillMaxSize()
        .padding(28.dp)
        .background(Color.White))
        {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center) {

            TextComponentUpper1(value = "Hey there, ")
            TextComponentUpper2(value = "Create an account")
            Spacer(modifier = Modifier.height(30.dp))
            MyTextField(labelValue = "Name", painterResource(id =R.drawable.usericon ))
            Spacer(modifier = Modifier.height(15.dp))
            MyTextField(labelValue = "E-mail", painterResource(id =R.drawable.emailicon ))
            Spacer(modifier = Modifier.height(15.dp))
            PasswordTextField(
                labelValue = "Password",
                painterResource = painterResource(id = R.drawable.passwordicon )
            )
        }

    }

}
@Composable
fun TextComponentUpper1(value: String){
    Text(text = value,
        modifier = Modifier
            .fillMaxWidth()

            .heightIn(min = 40.dp)
        ,style= TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal),
        textAlign = TextAlign.Center

    )

}
@Composable
fun TextComponentUpper2(value:String){
    Text(text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn()
            ,
        style = TextStyle(fontStyle =FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 30.sp ),
        textAlign = TextAlign.Center

    )

}


@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(labelValue: String, painterResource: Painter) {
    val textValue = remember { mutableStateOf("") }

    OutlinedTextField(
        modifier =Modifier.fillMaxWidth()
            ,

        label = { Text(text = labelValue) },
        value = textValue.value,
        onValueChange = { textValue.value = it },
        keyboardOptions = KeyboardOptions.Default,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = primaryLight,
            focusedLabelColor = secondaryLight,
            cursorColor = primaryLight,
            containerColor = Color(0xFFE0E0E0)


        )

        ,
        leadingIcon = {
            Icon(painter = painterResource, contentDescription ="" ,
                modifier = Modifier.size(35.dp))
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(labelValue: String, painterResource: Painter) {
    val password = remember { mutableStateOf("") }
    val passwordVisible= remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier =Modifier.fillMaxWidth()
        ,

        label = { Text(text = labelValue) },
        value = password.value,
        onValueChange = { password.value = it },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = primaryLight,
            focusedLabelColor = secondaryLight,
            cursorColor = primaryLight,
            containerColor = Color(0xFFE0E0E0)


        )

        ,
        leadingIcon = {
            Icon(painter = painterResource, contentDescription ="" ,
                modifier = Modifier.size(35.dp))
        },
        trailingIcon = {
            val iconImage= if(passwordVisible.value){
                Icons.Filled.Visibility

            }
            else{
                Icons.Filled.VisibilityOff
            }
            var description=if(passwordVisible.value){
                "Hide password"
            }
            else{
                "Show password"
            }

            IconButton(onClick ={
                passwordVisible.value=!passwordVisible.value
            } ){
                Icon(imageVector = iconImage, contentDescription = "")

            }

        },
        visualTransformation = if(passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()

    )

}