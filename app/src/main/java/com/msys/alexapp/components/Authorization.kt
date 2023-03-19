package com.msys.alexapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.services.User
import com.msys.alexapp.services.usersFlow
import com.msys.alexapp.ui.theme.AlexAppTheme

@Composable
fun Authorization(reportUserID: (String) -> Unit) {
  val users by usersFlow.collectAsState(initial = listOf())
  UserPicker(users = users, reportUserID = reportUserID)
}

@Composable
fun UserPicker(users: List<User>, reportUserID: (String) -> Unit) {
  LazyColumn { items(users) { UserCard(it, reportUserID) } }
}

@Composable
fun UserCard(user: User, reportUserID: (String) -> Unit) {
  Text(
    text = user.name,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { reportUserID(user.id) }
      .padding(5.dp)
  )
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