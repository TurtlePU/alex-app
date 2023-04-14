package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Report
import com.msys.alexapp.data.Role
import com.msys.alexapp.data.Role.*
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*

interface AlexAppService : AuthorizationService {
  fun invitationsFrom(role: Role): Flow<List<String>>
  fun juryService(stageID: String): JuryService
  fun stagePreparationService(adminID: String): StagePreparationService
  fun stageService(adminID: String): StageService
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      Authorization { role ->
        navController.navigate(role.toString()) {
          popUpTo("authorization") { inclusive = true }
        }
      }
    }
    navigation(route = ADMIN.toString(), startDestination = "") {
    }
    navigation(route = STAGE.toString(), startDestination = "invitations") {
      composable("invitations") {
        Invitations(invitationsFrom(ADMIN)) { id ->
          navController.navigate("list/$id") {
            launchSingleTop = true
          }
        }
      }
      composable("list/{adminID}") { backStack ->
        val adminID = backStack.arguments!!.getString("adminID")!!
        stagePreparationService(adminID).PerformanceList {
          navController.navigate("stage/$adminID") {
            restoreState = true
          }
        }
      }
      composable("stage/{adminID}") { backStack ->
        val adminID = backStack.arguments!!.getString("adminID")!!
        stageService(adminID).Carousel {
          navController.navigate("list/$adminID")
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
        juryService(backStack.arguments!!.getString("stageID")!!).Carousel()
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
      override fun juryService(stageID: String) = object : JuryService {
        override val currentPerformance: Flow<Performance> get() = flowOf()
        override val juryAdvice: Flow<Advice> get() = flowOf(Advice(currentDate()))
        override fun isEvaluated(id: String): Flow<Boolean> = flowOf(false)
        override fun averageRating(id: String): Flow<Double?> = flowOf(null)
        override suspend fun sendInvitation() {}
        override suspend fun evaluate(id: String, report: Report) {}
      }

      override fun stagePreparationService(adminID: String) = object : StagePreparationService {
        override val performancesFlow: Flow<List<Performance>> get() = flowOf()
        override val stagedFlow: Flow<List<String>> get() = flowOf()
        override suspend fun sendInvitations() {}
        override suspend fun newPerformance(performance: Performance) {}
        override suspend fun appendToStage(stage: List<String>) {}
      }

      override fun stageService(adminID: String) = object : StageService {
        override val canCommentFlow: Flow<Boolean> get() = flowOf()
        override val firstStagedPerformance: Flow<Performance?> get() = flowOf()
        override val nextStagedPerformance: Flow<String?> get() = flowOf()
        override fun reportsFlow(performanceID: String): Flow<Map<String, Report?>> = flowOf()
        override suspend fun setCanComment(canComment: Boolean) {}
        override suspend fun setCurrent(performance: Performance, deadline: Date) {}
        override suspend fun sendAverageRating(performanceID: String, averageRating: Double?) {}
        override suspend fun publishComments(
          performanceID: String,
          comments: Map<String, String>
        ) {
        }
      }
    }.NavComposable()
  }
}