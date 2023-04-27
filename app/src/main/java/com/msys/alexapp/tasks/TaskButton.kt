package com.msys.alexapp.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TaskButton(options: Task, text: String) {
  val progress by options.progressFlow.collectAsStateWithLifecycle(initialValue = 0f)
  Button(onClick = options.start) {
    Column {
      Text(text = text)
      LinearProgressIndicator(progress = progress.coerceIn(0f, 1f))
    }
  }
}

@Composable
fun dummyTask(steps: Int = 100, step: Duration = 100.milliseconds): Task {
  val progress = MutableStateFlow(0f)
  val scope = rememberCoroutineScope()
  val job: suspend CoroutineScope.() -> Unit = {
    for (i in 0..steps) {
      progress.value = 1f * i / steps
      delay(step)
    }
  }
  return Task(progress) { scope.launch(block = job) }
}

@Preview
@Composable
fun TaskButtonPreview() {
  TaskButton(dummyTask(), "Click me")
}