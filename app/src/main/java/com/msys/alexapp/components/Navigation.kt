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

interface AlexAppService : AuthorizationService {
  fun invitationsFrom(role: Role): Flow<List<String>>
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      Authorization { role -> navController.navigate("start/$role") }
    }
    composable("start/admin") {}
    composable("start/stage") {
      Invitations(invitationsFrom(ADMIN)) { id ->
        navController.navigate("carousel/stage/$id")
      }
    }
    composable("start/jury") {
      Invitations(invitationsFrom(STAGE)) { id ->
        navController.navigate("carousel/jury/$id")
      }
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