package com.msys.alexapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.launch

interface AuthorizationCallback {
  fun reportJuryID(uid: String)
  fun reportStageID(uid: String)
  suspend fun signIn(email: String, password: String): String
}

@Composable
fun AuthorizationCallback.Authorization() {
  var email: String? by rememberSaveable { mutableStateOf(null) }
  var password: String? by rememberSaveable { mutableStateOf(null) }
  var passwordHidden: Boolean by rememberSaveable { mutableStateOf(true) }
  var inProgress: Boolean by rememberSaveable { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val tryLogin: ((String) -> Unit) -> Unit = { report ->
    val mail = email
    val pass = password
    if (mail != null && pass != null && !inProgress) {
      inProgress = true
      scope.launch {
        try {
          report(signIn(mail, pass))
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
        IconButton(onClick = { passwordHidden = !passwordHidden }) {}
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
        onClick = { tryLogin(::reportJuryID) },
        modifier = Modifier.weight(1f),
        enabled = enabled,
      ) {
        Text(text = "Я жюри")
      }
      Spacer(modifier = Modifier.padding(5.dp))
      OutlinedButton(
        onClick = { tryLogin(::reportStageID) },
        modifier = Modifier.weight(1f),
        enabled = enabled,
      ) {
        Text(text = "Я выпускающий")
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AuthorizationPreview() {
  AlexAppTheme {
    object : AuthorizationCallback {
      override fun reportJuryID(uid: String) {}
      override fun reportStageID(uid: String) {}
      override suspend fun signIn(email: String, password: String) = email
    }.Authorization()
  }
}