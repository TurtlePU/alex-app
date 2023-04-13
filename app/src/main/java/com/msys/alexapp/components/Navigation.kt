package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msys.alexapp.data.Role
import com.msys.alexapp.data.Role.*
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AlexAppService {
  suspend fun signIn(email: String, password: String)
  fun invitationsFrom(role: Role): Flow<List<String>>
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      object : AuthorizationService {
        override suspend fun signIn(email: String, password: String) {
          this@NavComposable.signIn(email, password)
        }

        override fun become(role: Role) = navController.navigate("start/$role")
      }.Authorization()
    }
    composable("start/admin") {}
    composable("start/stage") {
      Invitations(invitationsFrom(ADMIN))
    }
    composable("start/jury") {
      Invitations(invitationsFrom(STAGE))
    }
    composable("carousel") {
      Carousel()
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  AlexAppTheme {
    object : AlexAppService {
      override suspend fun signIn(email: String, password: String) {}
      override fun invitationsFrom(role: Role) = flowOf(listOf("wow"))
    }.NavComposable()
  }
}