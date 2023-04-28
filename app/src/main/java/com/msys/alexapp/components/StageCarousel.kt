package com.msys.alexapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.common.View
import com.msys.alexapp.components.common.currentDate
import com.msys.alexapp.components.common.defaultTimeout
import com.msys.alexapp.components.common.plus
import com.msys.alexapp.data.JuryReport
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Summary.Companion.matching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

data class JuryNote(
  val nickname: String,
  val report: JuryReport?,
)

interface StageService {
  val canCommentFlow: Flow<Boolean>
  val degreeFlow: Flow<SortedMap<Double, String>>
  val firstStagedPerformance: Flow<Pair<String, Performance>?>
  val nextStagedPerformance: Flow<String?>
  val juryIDs: Flow<List<String>>
  fun readNote(juryID: String, performanceID: String): Flow<JuryNote?>
  suspend fun dropStaged(key: String)
  suspend fun setCanComment(canComment: Boolean)
  suspend fun setCurrent(performance: Performance, deadline: Date)
  suspend fun fetchDeadline(): Date
  suspend fun sendAverageRating(performanceID: String, averageRating: Double)
  suspend fun publishComment(performanceID: String, juryNickname: String, comment: String)
}

@Composable
fun StageService.Carousel(finishStage: () -> Unit) {
  val canComment by canCommentFlow.collectAsStateWithLifecycle(initialValue = false)
  val jury by juryIDs.collectAsStateWithLifecycle(initialValue = listOf())
  LaunchedEffect(canComment) { setCanComment(canComment) }
  firstStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
    ?.let { (key, perf) -> PerformanceDashboard(perf, jury, finishStage) { dropStaged(key) } }
    ?: FinishStage(finishStage)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageService.PerformanceDashboard(
  performance: Performance,
  juryIDs: List<String>,
  finishStage: () -> Unit,
  dropStaged: suspend () -> Unit,
) {
  val deadline = remember { currentDate() + defaultTimeout }
  LaunchedEffect(performance) { setCurrent(performance, deadline) }
  var canFinish by rememberSaveable { mutableStateOf(false) }
  var averageRating by rememberSaveable { mutableStateOf(Double.NaN) }
  LaunchedEffect(averageRating) { sendAverageRating(performance.id, averageRating) }
  val scope = rememberCoroutineScope()
  val degree by degreeFlow.collectAsStateWithLifecycle(initialValue = sortedMapOf())
  performance.View(
    deadline = deadline,
    cornerButton = {
      Button(onClick = { scope.launch { dropStaged() } }) {
        Icon(
          imageVector = Icons.Filled.Delete,
          contentDescription = stringResource(R.string.skip_participant)
        )
      }
    },
    bottomBar = {
      RatingBar(averageRating, if (averageRating.isNaN()) "" else degree.matching(averageRating))
    },
    floatingActionButton = {
      nextStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
        ?.let { id -> NextButton(id, canFinish) { scope.launch { dropStaged() } } }
        ?: FinishButton(canFinish) {
          scope.launch { dropStaged(); finishStage() }
        }
    }
  ) {
    FlowColumn {
      val list = mutableListOf<Double>()
      var allRated = true
      for (juryID in juryIDs) {
        val note by readNote(juryID, performance.id).collectAsStateWithLifecycle(null)
        Card {
          Text(text = note?.nickname ?: stringResource(R.string.default_jury_nickname))
          Text(text = note?.report?.rating?.toString() ?: "")
        }
        allRated = allRated && note?.report?.rating != null
        note?.report?.rating?.let { list.add(it) }
        LaunchedEffect(note?.nickname, note?.report?.comment) {
          note?.report?.comment?.let { publishComment(performance.id, note!!.nickname, it) }
        }
      }
      canFinish = allRated
      val avg = list.average()
      averageRating = if (avg.isNaN()) avg else (avg * 100).toLong() * .01
    }
  }
}

@Composable
fun RatingBar(averageRating: Double, degree: String) {
  if (!averageRating.isNaN()) {
    Row {
      Text(text = averageRating.toString())
      Text(text = degree)
    }
  }
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