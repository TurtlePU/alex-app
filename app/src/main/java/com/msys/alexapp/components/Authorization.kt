package com.msys.alexapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

interface AuthorizationCallback {
  fun reportJuryID(uid: String)
  fun reportStageID(uid: String)
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
          val res = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(mail, pass)
            .await()
          report(res.user!!.uid)
        } finally {
          inProgress = false
        }
      }
    }
  }

  Column {
    TextField(
      value = email ?: "",
      onValueChange = { email = it },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    TextField(
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
    Row {
      Button(onClick = { tryLogin(::reportJuryID) }) {
        Text(text = "Войти как жюри")
      }
      Button(onClick = { tryLogin(::reportStageID) }) {
        Text(text = "Войти как выпускающий")
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
    }.Authorization()
  }
}