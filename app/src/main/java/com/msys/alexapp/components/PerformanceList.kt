package com.msys.alexapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface StagePreparationService {
  val performancesFlow: Flow<List<Performance>>
  val stagedFlow: Flow<List<String>>
  suspend fun sendInvitations()
  suspend fun newPerformance(performance: Performance)
  suspend fun appendToStage(stage: List<String>)
}

enum class Tabs {
  STAGING, RATED;

  val text: String
    get() = when (this) {
      STAGING -> TODO()
      RATED -> TODO()
    }

  val icon: ImageVector
    get() = when (this) {
      STAGING -> TODO()
      RATED -> TODO()
    }
}

@Composable
fun StagePreparationService.PerformanceList(startStage: () -> Unit) {
  var ready by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(true) {
    sendInvitations()
    ready = true
  }
  val performances by performancesFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val staged by stagedFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val stagedSet = staged.toSet()
  val onStage = rememberSaveable { mutableStateMapOf<String, Unit>() }
  val isStaged: (String) -> Boolean = { stagedSet.contains(it) || onStage.containsKey(it) }
  var currentTab by remember { mutableStateOf(Tabs.STAGING) }
  Scaffold(
    topBar = {
      TabRow(selectedTabIndex = currentTab.ordinal) {
        for (tab in Tabs.values()) {
          Tab(
            selected = currentTab == tab,
            onClick = { currentTab = tab },
            text = { Text(text = tab.text) },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = null
              )
            },
          )
        }
      }
    },
    floatingActionButton = {
      if (currentTab == Tabs.STAGING && ready && !onStage.isEmpty()) {
        val scope = rememberCoroutineScope()
        FloatingActionButton(
          onClick = { scope.launch { appendToStage(onStage.keys.toList()); startStage() } },
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
                if (isStaged(id)) onStage.remove(id)
                else onStage[id] = Unit
              }
              .background(
                if (isStaged(id)) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.background
              )
          ) {
            Text(text = id)
            Text(text = name)
            Text(text = performance)
          }
        }
      }
      item {
        val initialID = performances.maxOfOrNull { it.id.toLong() + 1 } ?: 0
        NewPerformance(initialID) { newPerformance(it) }
      }
    }
  }
}

@Composable
fun NewPerformance(initialID: Long, send: suspend (Performance) -> Unit) {
  var isDraft by rememberSaveable { mutableStateOf(false) }
  if (isDraft) {
    var id: Long? by rememberSaveable { mutableStateOf(initialID) }
    var name: String? by rememberSaveable { mutableStateOf(null) }
    var performance: String? by rememberSaveable { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    Row {
      Text(text = "#")
      TextField(value = id?.toString() ?: "", onValueChange = { id = it.toLongOrNull() })
      TextField(value = name ?: "", onValueChange = { name = it })
      TextField(value = performance ?: "", onValueChange = { performance = it })
      IconButton(
        onClick = {
          scope.launch {
            send(anonymousPerformance(id!!, name!!, performance!!))
            isDraft = false
          }
        },
        enabled = (id?.let { it >= initialID } ?: false) && name != null && performance != null
      ) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = stringResource(R.string.add_performance),
        )
      }
      IconButton(onClick = { isDraft = false }) {
        Icon(
          imageVector = Icons.Filled.Cancel,
          contentDescription = stringResource(R.string.cancel_new_performance),
        )
      }
    }
  } else {
    IconButton(
      onClick = { isDraft = true },
    ) {
      Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = stringResource(R.string.add_performance),
      )
    }
  }
}

fun anonymousPerformance(id: Long, name: String, performance: String) = Performance(
  id = id.toString(),
  name = name,
  performance = performance,
  null, null, null, null
)