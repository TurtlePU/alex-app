package com.msys.alexapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.R
import com.msys.alexapp.services.Role
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.launch

interface AuthorizationCallback {
  fun become(role: Role)
  suspend fun signIn(email: String, password: String)
}

@Composable
fun AuthorizationCallback.Authorization() {
  var email: String? by rememberSaveable { mutableStateOf(null) }
  var password: String? by rememberSaveable { mutableStateOf(null) }
  var passwordHidden: Boolean by rememberSaveable { mutableStateOf(true) }
  var inProgress: Boolean by rememberSaveable { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val signInAs: (Role) -> Unit = { role ->
    val mail = email
    val pass = password
    if (mail != null && pass != null && !inProgress) {
      inProgress = true
      scope.launch {
        try {
          signIn(mail, pass)
          become(role)
        } finally {
          inProgress = false
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(5.dp),
    verticalArrangement = Arrangement.Center,
  ) {
    OutlinedTextField(
      value = email ?: "",
      onValueChange = { email = it },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    Spacer(modifier = Modifier.padding(3.dp))
    OutlinedTextField(
      value = password ?: "",
      onValueChange = { password = it },
      modifier = Modifier.fillMaxWidth(),
      trailingIcon = {
        IconButton(onClick = { passwordHidden = !passwordHidden }) {
          Icon(
            imageVector =
            if (passwordHidden) Icons.Filled.VisibilityOff
            else Icons.Filled.Visibility,
            contentDescription = stringResource(R.string.show_password),
          )
        }
      },
      visualTransformation =
      if (passwordHidden) PasswordVisualTransformation()
      else VisualTransformation.None,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
    Spacer(modifier = Modifier.padding(1.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
      val enabled = email != null && password != null && !inProgress
      OutlinedButton(
        onClick = { signInAs(Role.JURY) },
        modifier = Modifier.weight(1f),
        enabled = enabled,
      ) {
        Text(text = stringResource(R.string.login_jury))
      }
      Spacer(modifier = Modifier.padding(5.dp))
      OutlinedButton(
        onClick = { signInAs(Role.STAGE) },
        modifier = Modifier.weight(1f),
        enabled = enabled,
      ) {
        Text(text = stringResource(R.string.login_stage))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AuthorizationPreview() {
  AlexAppTheme {
    object : AuthorizationCallback {
      override fun become(role: Role) {}
      override suspend fun signIn(email: String, password: String) {}
    }.Authorization()
  }
}