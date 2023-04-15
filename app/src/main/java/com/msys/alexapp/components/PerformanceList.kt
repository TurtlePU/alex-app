package com.msys.alexapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.Tabs.*
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class StageReport(
  val averageRating: Double,
  val comments: Map<String, String>,
)

interface StagePreparationService {
  val performancesFlow: Flow<List<Performance>>
  val stagedFlow: Flow<List<String>>
  val reportFlow: Flow<Map<String, StageReport>>
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
  val reports by reportFlow.collectAsStateWithLifecycle(initialValue = mapOf())
  var currentTab by remember { mutableStateOf(STAGING) }
  Scaffold(
    topBar = {
      TabRow(selectedTabIndex = currentTab.ordinal) {
        for (tab in values()) {
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
      if (currentTab == STAGING && ready && !onStage.isEmpty()) {
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
      STAGING -> StagingList(
        performances = performances.filter { !reports.containsKey(it.id) },
        onClick = { id ->
          if (isStaged(id)) onStage.remove(id)
          else onStage[id] = Unit
        },
        background = { id ->
          if (isStaged(id)) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.background
        },
        newPerformanceInitialID = performances.maxOfOrNull { it.id.toLong() + 1 } ?: 0,
        newPerformance = { newPerformance(it) },
        modifier = modifier,
      )
      RATED -> ReportList(
        reports = performances.mapNotNull { perf -> reports[perf.id]?.let { perf to it } },
        modifier = modifier
      )
    }
  }
}

@Composable
fun StagingList(
  performances: List<Performance>,
  onClick: (String) -> Unit,
  background: @Composable (String) -> Color,
  newPerformanceInitialID: Long,
  newPerformance: suspend (Performance) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    items(items = performances, key = { it.id }) {
      it.Card(modifier = Modifier
        .clickable { onClick(it.id) }
        .background(background(it.id)))
    }
    item { NewPerformance(newPerformanceInitialID, newPerformance) }
  }
}

@Composable
fun ReportList(reports: List<Pair<Performance, StageReport>>, modifier: Modifier = Modifier) {
  var expandedID: String? by remember { mutableStateOf(null) }
  val manager = LocalClipboardManager.current
  LazyColumn(modifier = modifier) {
    items(items = reports, key = { it.first.id }) { pair ->
      pair.Card(
        onClick = { expandedID = it },
        isExpanded = { expandedID == it },
        copyText = manager::setText,
      )
    }
  }
}

@Composable
fun Pair<Performance, StageReport>.Card(
  onClick: (String) -> Unit,
  isExpanded: (String) -> Boolean,
  copyText: (AnnotatedString) -> Unit,
) {
  Column {
    first.Card(modifier = Modifier.clickable { onClick(first.id) })
    val transitionState = remember { MutableTransitionState(isExpanded(first.id)) }
    AnimatedVisibility(visibleState = transitionState) { second.Card(copyText) }
  }
}

@Composable
fun Performance.Card(modifier: Modifier = Modifier) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(text = id)
    Text(text = name)
    Text(text = performance)
  }
}

@Composable
fun StageReport.Card(copyText: (AnnotatedString) -> Unit) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Text(text = averageRating.toString())
    val commentText = comments.entries.joinToString(separator = "\n") { it.run { "$key: $value" } }
    TextField(
      value = commentText,
      onValueChange = {},
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { copyText(AnnotatedString(commentText)) }) {
          Icon(
            imageVector = Icons.Filled.CopyAll,
            contentDescription = stringResource(R.string.copy_comments),
          )
        }
      },
      singleLine = false,
    )
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