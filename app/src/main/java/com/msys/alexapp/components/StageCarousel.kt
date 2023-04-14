package com.msys.alexapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

data class JuryNote(
  val nickname: String,
  val report: Report?,
)

interface StageService {
  val canCommentFlow: Flow<Boolean>
  val firstStagedPerformance: Flow<Pair<String, Performance>?>
  val nextStagedPerformance: Flow<String?>
  fun performanceDashboard(id: String): Flow<Map<String, JuryNote>>
  suspend fun dropStaged(key: String)
  suspend fun setCanComment(canComment: Boolean)
  suspend fun setCurrent(performance: Performance, deadline: Date)
  suspend fun sendAverageRating(performanceID: String, averageRating: Double)
  suspend fun publishComments(performanceID: String, comments: Map<String, String>)
}

@Composable
fun StageService.Carousel(finishStage: () -> Unit) {
  val canComment by canCommentFlow.collectAsStateWithLifecycle(initialValue = false)
  LaunchedEffect(canComment) { setCanComment(canComment) }
  firstStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
    ?.let { (key, performance) ->
      PerformanceDashboard(performance, finishStage) { dropStaged(key) }
    }
    ?: FinishStage(finishStage)
}

@Composable
fun StageService.PerformanceDashboard(
  performance: Performance,
  finishStage: () -> Unit,
  finishPerformance: suspend () -> Unit,
) {
  val deadline = rememberSaveable { currentDate().time + timeout.inWholeMilliseconds }
  LaunchedEffect(true) { setCurrent(performance, Date(deadline)) }
  val dashboard by performanceDashboard(performance.id).collectAsStateWithLifecycle(mapOf())
  val canFinish = dashboard.all { it.value.report != null }
  val averageRating = dashboard.mapNotNull { it.value.report?.rating }.average()
  LaunchedEffect(averageRating) { sendAverageRating(performance.id, averageRating) }
  performance.View(
    deadline = Date(deadline),
    bottomBar = { RatingBar(averageRating) },
    floatingActionButton = {
      val scope = rememberCoroutineScope()
      nextStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
        ?.let { id -> NextButton(id, canFinish) { scope.launch { finishPerformance() } } }
        ?: FinishButton(canFinish) { scope.launch { finishPerformance(); finishStage() } }
    }
  ) {
    JuryRow(dashboard.values)
  }
}

@Composable
fun RatingBar(averageRating: Double) {
}

@Composable
fun JuryRow(dashboard: Collection<JuryNote>) {
}

@Composable
fun NextButton(id: String, enabled: Boolean, onClick: () -> Unit) {
  ExtendedFloatingActionButton(
    text = { Text(text = id) },
    icon = {
      Icon(
        imageVector = Icons.Filled.NavigateNext,
        contentDescription = stringResource(R.string.next_performance),
      )
    },
    onClick = { if (enabled) onClick() },
    containerColor = if (enabled) FloatingActionButtonDefaults.containerColor else Color.Gray
  )
}

@Composable
fun FinishButton(enabled: Boolean, onClick: () -> Unit) {
  FloatingActionButton(
    onClick = { if (enabled) onClick() },
    containerColor = if (enabled) FloatingActionButtonDefaults.containerColor else Color.Gray
  ) {
    Icon(
      imageVector = Icons.Filled.Check,
      contentDescription = stringResource(R.string.finish_stage),
    )
  }
}

@Composable
fun FinishStage(finishStage: () -> Unit) {
  Column {
    Text(text = stringResource(R.string.no_performance))
    Button(onClick = finishStage) {
      Text(text = stringResource(R.string.back_to_list))
    }
  }
}