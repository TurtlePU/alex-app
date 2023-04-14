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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Advice
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow
import java.util.*

interface StageService {
  val canCommentFlow: Flow<Boolean>
  val firstStagedPerformance: Flow<Performance?>
  val nextStagedPerformance: Flow<String?>
  suspend fun sendAdvice(advice: Advice)
  suspend fun setCurrent(performance: Performance)
}

@Composable
fun StageService.Carousel(finishStage: () -> Unit) {
  val performance by firstStagedPerformance.collectAsStateWithLifecycle(initialValue = null)
  performance?.let {
    LaunchedEffect(true) { setCurrent(it) }
    val deadline = rememberSaveable { currentDate().time + timeout.inWholeMilliseconds }
    val canComment by canCommentFlow.collectAsStateWithLifecycle(initialValue = false)
    LaunchedEffect(canComment) { sendAdvice(Advice(Date(deadline), canComment)) }
    it.View(
      deadline = Date(deadline),
      floatingActionButton = {
        val nextID by nextStagedPerformance.collectAsStateWithLifecycle(initialValue = null)
        val finishPerformance: () -> Unit = { /*TODO*/ }
        nextID?.let { id ->
          ExtendedFloatingActionButton(
            text = { Text(text = id) },
            icon = {
              Icon(
                imageVector = Icons.Filled.NavigateNext,
                contentDescription = stringResource(R.string.next_performance),
              )
            },
            onClick = finishPerformance,
          )
        }
          ?: FloatingActionButton(onClick = { finishPerformance(); finishStage() }) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = stringResource(R.string.finish_stage),
            )
          }
      }
    ) {
      /*TODO*/
    }
  } ?: FinishStage(finishStage)
}

@Composable
fun FinishStage(finishStage: () -> Unit) {
}