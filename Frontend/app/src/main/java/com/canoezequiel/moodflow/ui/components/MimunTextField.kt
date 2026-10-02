package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R
import com.canoezequiel.moodflow.ui.theme.MimunBackButton
import com.canoezequiel.moodflow.ui.theme.MimunBorder
import com.canoezequiel.moodflow.ui.theme.MimunSurface
import com.canoezequiel.moodflow.ui.theme.MimunTextSecondary

@Composable
fun MimunTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    placeHolderText: String = "",
    isPassword: Boolean = false,
    iconOT: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {

    //Estado local para recordar si el usuario quiere ver la contraseña o no
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        //Valor que ingresa el usuario
        value = value,
        //Cada iteracion del usuario se guarda
        onValueChange = { newText ->
            onValueChange(newText)
        },
        //Texto del contenedor(Sube arriba a la izquierda)
        label = {
            Text(text = labelText)
        },
        //Texto que aparece mientras el contenedor este vacio.
        placeholder = {
            if (placeHolderText.isNotEmpty()) Text(placeHolderText)
        },
        colors = OutlinedTextFieldDefaults.colors(
            //Color del texto
            focusedTextColor = MimunTextSecondary,
            unfocusedTextColor = MimunTextSecondary,
            //Background del contenedor
            focusedContainerColor = MimunBackButton,
            unfocusedContainerColor = MimunBackButton,
            //Borde
            focusedBorderColor = MimunBorder,
            unfocusedBorderColor = MimunBorder,
            //Texto Label
            unfocusedLabelColor = MimunTextSecondary,
            focusedLabelColor = MimunTextSecondary
        ),
        //Para que el usuario solo escriba en una sola linea.
        singleLine = true,
        keyboardOptions = keyboardOptions,
        //Si la password isVisible and !isVisible, oculta text con asteriscos
        visualTransformation = if (isPassword && !isPasswordVisible){
            PasswordVisualTransformation()
        }else{
            VisualTransformation.None
        },
        //Icono principio del campo
        leadingIcon = {
            Icon(
                imageVector = iconOT,
                contentDescription = null
            )
        },
        //Icono del final
        trailingIcon = {
            if (isPassword){
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.ic_password_inv
                            else R.drawable.ic_password
                        ),
                        contentDescription =
                            if (isPasswordVisible) "Ocultar contraseña"
                            else "Mostar contraseña",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewOT() {
    MaterialTheme {
        MimunTextField(
            value = "",
            onValueChange = {},
            labelText = "Email",
            isPassword = false,
            placeHolderText = "Ingresa tu email",
            iconOT = Icons.Default.Email
        )
    }
}
@Preview
@Composable
fun PreviewOTPassword() {
    MaterialTheme {
        MimunTextField(
            value = "",
            onValueChange = {},
            labelText = "Contraseña",
            isPassword = true,
            placeHolderText = "Ingresa tu contraseña",
            iconOT = Icons.Default.Lock
        )
    }
}