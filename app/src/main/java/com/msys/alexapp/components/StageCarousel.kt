package com.msys.alexapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

interface StageService {
  val canCommentFlow: Flow<Boolean>
  val firstStagedPerformance: Flow<Performance?>
  val nextStagedPerformance: Flow<String?>
  suspend fun setCanComment(canComment: Boolean)
  suspend fun setCurrent(performance: Performance, deadline: Date)
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
  performance.View(
    deadline = Date(deadline),
    floatingActionButton = {
      val finishPerformance: suspend () -> Unit = { /*TODO*/ }
      val scope = rememberCoroutineScope()
      nextStagedPerformance.collectAsStateWithLifecycle(initialValue = null).value
        ?.let { id -> NextButton(id) { scope.launch { finishPerformance() } } }
        ?: FinishButton { scope.launch { finishPerformance(); finishStage() } }
    }
  ) {
    /*TODO*/
  }
}

@Composable
fun NextButton(id: String, onClick: () -> Unit) {
  ExtendedFloatingActionButton(
    text = { Text(text = id) },
    icon = {
      Icon(
        imageVector = Icons.Filled.NavigateNext,
        contentDescription = stringResource(R.string.next_performance),
      )
    },
    onClick = onClick,
  )
}

@Composable
fun FinishButton(onClick: () -> Unit) {
  FloatingActionButton(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Check,
      contentDescription = stringResource(R.string.finish_stage),
    )
  }
}

@Composable
fun FinishStage(finishStage: () -> Unit) {
}