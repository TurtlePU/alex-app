package com.msys.alexapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow

interface StageService {
  val performancesFlow: Flow<List<Performance>>
  suspend fun sendInvitations()
  suspend fun newPerformance(performance: Performance)
}

@Composable
fun StageService.PerformanceList() {
  LaunchedEffect(true) { sendInvitations() }
  val performances by performancesFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val onStage = rememberSaveable { mutableStateMapOf<String, Unit>() }
  LazyColumn {
    items(performances) {
      it.run {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              if (onStage.containsKey(id)) onStage.remove(id)
              else onStage[id] = Unit
            }
            .background(
              if (onStage.containsKey(id)) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.background
            )
        ) {
          Text(text = id)
          Text(text = name)
          Text(text = performance)
        }
      }
    }
    item { NewPerformance { newPerformance(it) } }
  }
}

@Composable
fun NewPerformance(send: suspend (Performance) -> Unit) {
}