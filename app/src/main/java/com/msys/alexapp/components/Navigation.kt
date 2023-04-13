package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import com.msys.alexapp.data.Role.*
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AlexAppService : AuthorizationService {
  fun invitationsFrom(role: Role): Flow<List<String>>
  fun carouselService(stageID: String): CarouselService
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      Authorization { role -> navController.navigate(role.toString()) }
    }
    navigation(route = ADMIN.toString(), startDestination = "") {
    }
    navigation(route = STAGE.toString(), startDestination = "invitations") {
      composable("invitations") {
        Invitations(invitationsFrom(ADMIN)) { id ->
          navController.navigate("list/$id")
        }
      }
    }
    navigation(route = JURY.toString(), startDestination = "invitations") {
      composable("invitations") {
        Invitations(invitationsFrom(STAGE)) { id ->
          navController.navigate("carousel/$id")
        }
      }
      composable("carousel/{stageID}") { backStack ->
        carouselService(backStack.arguments!!.getString("stageID")!!).Carousel()
      }
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
      override fun carouselService(stageID: String) = object : CarouselService {
        override val currentPerformance: Flow<Performance> get() = flowOf()
        override val canComment: Flow<Boolean> = flowOf(true)
        override fun isEvaluated(id: String): Flow<Boolean> = flowOf(false)
        override suspend fun sendInvitation() {}
        override suspend fun evaluate(id: String, rating: Double, comment: String?) {}
      }
    }.NavComposable()
  }
}