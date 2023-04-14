package com.msys.alexapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface StageService {
  val performancesFlow: Flow<List<Performance>>
  suspend fun sendInvitations()
  suspend fun newPerformance(performance: Performance)
  suspend fun setStage(stage: List<String>)
}

@Composable
fun StageService.PerformanceList(startStage: () -> Unit) {
  var ready by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(true) {
    sendInvitations()
    ready = true
  }
  val performances by performancesFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val onStage = rememberSaveable { mutableStateMapOf<String, Unit>() }
  val scope = rememberCoroutineScope()
  Scaffold(
    floatingActionButton = {
      if (ready && !onStage.isEmpty()) {
        FloatingActionButton(
          onClick = { scope.launch { setStage(onStage.keys.toList()); startStage() } },
        ) {
          Icon(
            imageVector = Icons.Filled.Start,
            contentDescription = stringResource(R.string.start_stage),
          )
        }
      }
    }
  ) { padding ->
    LazyColumn(modifier = Modifier.padding(padding)) {
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
}

@Composable
fun NewPerformance(send: suspend (Performance) -> Unit) {
}