package com.msys.alexapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
  val firstStagedPerformance: Flow<Pair<Long, Performance>?>
  suspend fun sendAdvice(advice: Advice)
  suspend fun setCurrent(performance: Performance)
}

@Composable
fun StageService.Carousel(finishStage: () -> Unit) {
  val performance by firstStagedPerformance.collectAsStateWithLifecycle(initialValue = null)
  performance?.let {
    LaunchedEffect(true) { setCurrent(it.second) }
    val deadline = rememberSaveable { currentDate().time + timeout.inWholeMilliseconds }
    val canComment by canCommentFlow.collectAsStateWithLifecycle(initialValue = false)
    LaunchedEffect(canComment) { sendAdvice(Advice(Date(deadline), it.first, canComment)) }
    it.second.View(
      index = it.first,
      deadline = Date(deadline),
      floatingActionButton = {
        ExtendedFloatingActionButton(
          text = { /*TODO*/ },
          icon = {
            Icon(
              imageVector = Icons.Filled.NavigateNext,
              contentDescription = stringResource(R.string.next_performance),
            )
          },
          onClick = { /*TODO*/ },
        )
      }
    ) {
      /*TODO*/
    }
  } ?: FinishStage(finishStage)
}

@Composable
fun FinishStage(finishStage: () -> Unit) {
}