package com.msys.alexapp.components

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

interface StageService {
  val canCommentFlow: Flow<Boolean>
  val firstStagedPerformance: Flow<Performance?>
  val nextStagedPerformance: Flow<String?>
  fun reportsFlow(performanceID: String): Flow<Map<String, Report?>>
  suspend fun setCanComment(canComment: Boolean)
  suspend fun setCurrent(performance: Performance, deadline: Date)
  suspend fun sendAverageRating(performanceID: String, averageRating: Double?)
  suspend fun publishComments(performanceID: String, comments: Map<String, String>)
}

@Composable
fun StageService.Carousel(finishStage: () -> Unit) {
  firstStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
    ?.let { PerformanceDashboard(it, finishStage) }
    ?: FinishStage(finishStage)
}

@Composable
fun StageService.PerformanceDashboard(performance: Performance, finishStage: () -> Unit) {
  val deadline = rememberSaveable { currentDate().time + timeout.inWholeMilliseconds }
  LaunchedEffect(true) { setCurrent(performance, Date(deadline)) }
  val canComment by canCommentFlow.collectAsStateWithLifecycle(initialValue = false)
  LaunchedEffect(canComment) { setCanComment(canComment) }
  val reports by reportsFlow(performance.id).collectAsStateWithLifecycle(initialValue = mapOf())
  val averageRating = reports
    .mapNotNull { it.value?.rating }
    .run { if (isEmpty()) null else average() }
  LaunchedEffect(averageRating) { sendAverageRating(performance.id, averageRating) }
  performance.View(
    deadline = Date(deadline),
    bottomBar = { RatingBar(averageRating) },
    floatingActionButton = {
      val finishPerformance: suspend () -> Unit = {
        publishComments(
          performance.id,
          reports
            .mapNotNull { it.value?.comment?.let { comment -> it.key to comment } }
            .toMap()
        )
      }
      val enabled = reports.all { it.value != null }
      val scope = rememberCoroutineScope()
      nextStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
        ?.let { id -> NextButton(id, enabled) { scope.launch { finishPerformance() } } }
        ?: FinishButton(enabled) { scope.launch { finishPerformance(); finishStage() } }
    }
  ) { JuryRow(reports) }
}

@Composable
fun RatingBar(averageRating: Double?) {
}

@Composable
fun JuryRow(reports: Map<String, Report?>) {
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
}