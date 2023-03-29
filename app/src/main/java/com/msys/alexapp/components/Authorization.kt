package com.msys.alexapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.msys.alexapp.services.User
import com.msys.alexapp.services.usersFlow
import com.msys.alexapp.ui.theme.AlexAppTheme

@Composable
fun Authorization(reportUserID: (String) -> Unit) {
  var signedIn: Boolean by rememberSaveable { mutableStateOf(false) }
  if (signedIn) {
    val users by usersFlow.collectAsState(initial = listOf())
    UserPicker(users = users, reportUserID = reportUserID)
  } else {
    LaunchedEffect(true) {
      FirebaseAuth.getInstance().signInAnonymously()
      signedIn = true
    }
  }
}

@Composable
fun UserPicker(users: List<User>, reportUserID: (String) -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceEvenly
  ) {
    for (user in users) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .clickable { reportUserID(user.name) },
        contentAlignment = Alignment.Center,
      ) {
        Text(text = user.name)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun UserPickerPreview() {
  val exampleUsers = listOf(
    User("android", "Android"),
    User("ios", "iPhone")
  )
  AlexAppTheme { UserPicker(exampleUsers) {} }
}