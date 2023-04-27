package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.msys.alexapp.tasks.Task
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.JuryReport
import com.msys.alexapp.data.Role
import com.msys.alexapp.data.Role.*
import com.msys.alexapp.data.StageReport
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*

interface AlexAppService : AuthorizationService, AdminService {
  fun invitationsFrom(role: Role): Flow<List<String>>
  fun juryService(stageID: String): JuryService
  fun stagePreparationService(adminID: String): StagePreparationService
  fun stageService(adminID: String): StageService
}

@Composable
fun AlexAppService.NavComposable(
  loadParticipants: @Composable () -> Task,
  saveResults: @Composable () -> Task,
) {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      Authorization { role ->
        navController.navigate(role.toString())
      }
    }
    composable(ADMIN.toString()) { Admin(loadParticipants, saveResults) }
    navigation(route = STAGE.toString(), startDestination = "stage/invitations") {
      composable("stage/invitations") {
        Invitations(invitationsFrom(ADMIN)) { id ->
          navController.navigate("stage/list/$id") {
            launchSingleTop = true
          }
        }
      }
      composable("stage/list/{adminID}") { backStack ->
        val adminID = backStack.arguments!!.getString("adminID")!!
        stagePreparationService(adminID).PerformanceList {
          navController.navigate("stage/stage/$adminID") {
            restoreState = true
          }
        }
      }
      composable("stage/stage/{adminID}") { backStack ->
        val adminID = backStack.arguments!!.getString("adminID")!!
        stageService(adminID).Carousel {
          navController.popBackStack()
          navController.clearBackStack("stage/stage/$adminID")
        }
      }
    }
    navigation(route = JURY.toString(), startDestination = "jury/invitations") {
      composable("jury/invitations") {
        Invitations(invitationsFrom(STAGE)) { id ->
          navController.navigate("jury/carousel/$id")
        }
      }
      composable("jury/carousel/{stageID}") { backStack ->
        juryService(backStack.arguments!!.getString("stageID")!!).Carousel {
          navController.popBackStack("authorization", inclusive = false)
        }
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
      override val contactsFlow: Flow<Map<String, String>> get() = flowOf()
      override suspend fun setCanComment(canComment: Boolean) {}
      override suspend fun addContact(email: String, nickname: String) {}
      override suspend fun deleteContact(email: String) {}
      override suspend fun setStage(email: String) {}
      override fun invitationsFrom(role: Role) = flowOf(listOf("wow"))
      override fun juryService(stageID: String) = object : JuryService {
        override val currentPerformance: Flow<Performance> get() = flowOf()
        override val juryAdvice: Flow<Advice> get() = flowOf(Advice(currentDate()))
        override fun isEvaluated(id: String): Flow<Boolean> = flowOf(false)
        override fun averageRating(id: String): Flow<Double?> = flowOf(null)
        override suspend fun sendInvitation() {}
        override suspend fun evaluate(id: String, report: JuryReport) {}
      }

      override fun stagePreparationService(adminID: String) = object : StagePreparationService {
        override val performancesFlow: Flow<List<Performance>> get() = flowOf()
        override val stagedFlow: Flow<List<String>> get() = flowOf()
        override val reportFlow: Flow<Map<String, StageReport>> get() = flowOf()
        override suspend fun sendInvitations() {}
        override suspend fun dropCurrent() {}
        override suspend fun newPerformance(performance: Performance) {}
        override suspend fun appendToStage(stage: List<String>) {}
      }

      override fun stageService(adminID: String) = object : StageService {
        override val canCommentFlow: Flow<Boolean> get() = flowOf()
        override val firstStagedPerformance: Flow<Pair<String, Performance>?> get() = flowOf()
        override val nextStagedPerformance: Flow<String?> get() = flowOf()
        override val juryIDs: Flow<List<String>> get() = flowOf()
        override fun readNote(juryID: String, performanceID: String): Flow<JuryNote?> = flowOf()
        override suspend fun dropStaged(key: String) {}
        override suspend fun setCanComment(canComment: Boolean) {}
        override suspend fun setCurrent(performance: Performance, deadline: Date) {}
        override suspend fun sendAverageRating(performanceID: String, averageRating: Double) {}
        override suspend fun publishComment(
          performanceID: String,
          juryNickname: String,
          comment: String
        ) {
        }
      }
    }.NavComposable(
      loadParticipants = { Task(flowOf()) { } },
      saveResults = { Task(flowOf()) { } },
    )
  }
}