package com.msys.alexapp.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.Tabs.RATED
import com.msys.alexapp.components.Tabs.STAGING
import com.msys.alexapp.components.Tabs.values
import com.msys.alexapp.components.stage.cards.Card
import com.msys.alexapp.components.stage.cards.NewPerformance
import com.msys.alexapp.components.stage.cards.StagingCard
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.StageReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface StagePreparationService {
  val performancesFlow: Flow<List<Performance>>
  val stagedFlow: Flow<List<String>>
  val reportFlow: Flow<Map<String, StageReport>>
  suspend fun sendInvitations()
  suspend fun dropCurrent()
  suspend fun newPerformance(performance: Performance)
  suspend fun appendToStage(stage: List<String>)
}

enum class Tabs {
  STAGING, RATED;

  val text: Int
    @StringRes
    get() = when (this) {
      STAGING -> R.string.staging_text
      RATED -> R.string.rated_text
    }

  val icon: ImageVector
    get() = when (this) {
      STAGING -> Icons.Filled.Start
      RATED -> Icons.Filled.Archive
    }
}

@Composable
fun StagePreparationService.PerformanceList(startStage: () -> Unit) {
  var ready by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(true) {
    sendInvitations()
    dropCurrent()
    ready = true
  }
  val performances by performancesFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val staged by stagedFlow.collectAsStateWithLifecycle(initialValue = listOf())
  val stagedSet = staged.toSet()
  val onStage = remember { mutableStateMapOf<String, Unit>() }
  val reports by reportFlow.collectAsStateWithLifecycle(initialValue = mapOf())
  var currentTab by remember { mutableStateOf(STAGING) }
  Scaffold(
    topBar = {
      TabRow(selectedTabIndex = currentTab.ordinal) {
        for (tab in values()) {
          Tab(
            selected = currentTab == tab,
            onClick = { currentTab = tab },
            text = { Text(text = stringResource(tab.text)) },
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
      if (currentTab == STAGING && ready && (onStage.isNotEmpty() || staged.isNotEmpty())) {
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
    val modifier = Modifier.padding(padding)
    when (currentTab) {
      STAGING -> {
        val items = performances.filter { !reports.containsKey(it.id) }
        LazyColumn(modifier = modifier) {
          items(items = items, key = { it.id }) { performance ->
            performance.StagingCard(
              isStaged = { stagedSet.contains(it) || onStage.containsKey(it) },
              addStaged = { onStage[it] = Unit },
              removeStaged = onStage::remove,
            )
          }
          val initialID = performances.maxOfOrNull { it.id.toLong() + 1 } ?: 0
          item { NewPerformance(initialID, ::newPerformance) }
        }
      }

      RATED -> {
        val items = performances.mapNotNull { perf -> reports[perf.id]?.let { perf to it } }
        var expandedID: String? by remember { mutableStateOf(null) }
        LazyColumn(modifier = modifier) {
          items(items = items, key = { it.first.id }) { pair ->
            pair.Card(
              onClick = { expandedID = it },
              isExpanded = { expandedID == it },
            )
          }
        }
      }
    }
  }
}