package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import com.msys.alexapp.data.Advice
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow

interface StageService {
  val firstStagedPerformance: Flow<Pair<Long, Performance>?>
  suspend fun sendAdvice(advice: Advice)
  suspend fun setCurrent(performance: Performance)
}

@Composable
fun StageService.Carousel(finishStage: () ->  Unit) {}