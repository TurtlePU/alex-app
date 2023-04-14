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
  fun carouselService(stageID: String): JuryService
  fun stageService(adminID: String): StageService
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      Authorization { role -> navController.navigate(role.toString()) { popUpTo("") } }
    }
    navigation(route = ADMIN.toString(), startDestination = "") {
    }
    navigation(route = STAGE.toString(), startDestination = "invitations") {
      composable("invitations") {
        Invitations(invitationsFrom(ADMIN)) { id ->
          navController.navigate("list/$id")
        }
      }
      composable("list/{adminID}") { backStack ->
        val adminID = backStack.arguments!!.getString("adminID")!!
        stageService(adminID).PerformanceList {
          navController.navigate("stage/$adminID")
        }
      }
      composable("stage/{adminID}") {}
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
      override fun carouselService(stageID: String) = object : JuryService {
        override val currentPerformance: Flow<Performance> get() = flowOf()
        override val juryAdvice: Flow<Advice> get() = flowOf(Advice(currentDate()))
        override fun isEvaluated(id: String): Flow<Boolean> = flowOf(false)
        override fun averageRating(id: String): Flow<Double?> = flowOf(null)
        override suspend fun sendInvitation() {}
        override suspend fun evaluate(id: String, rating: Double, comment: String?) {}
      }
      override fun stageService(adminID: String) = object : StageService {
        override val performancesFlow: Flow<List<Performance>> get() = flowOf()
        override suspend fun sendInvitations() {}
        override suspend fun newPerformance(performance: Performance) {}
        override suspend fun setStage(stage: List<String>) {}
      }
    }.NavComposable()
  }
}