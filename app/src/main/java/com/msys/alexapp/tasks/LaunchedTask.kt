package com.msys.alexapp.tasks

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.Closeable

@Composable
fun <I, O, R : Closeable> launchedTask(
  contract: ActivityResultContract<I, O?>,
  input: I,
  resource: @Composable (O) -> R?,
  job: suspend ProducerScope<Float>.(R) -> Unit
): Task {
  var output: O? by rememberSaveable { mutableStateOf(null) }
  val picker = rememberLauncherForActivityResult(contract) { if (it != null) output = it }
  val result = output?.let { resource(it) }
  val flow = MutableStateFlow(0f)
  LaunchedEffect(result) {
    result?.use {
      callbackFlow {
        withContext(Dispatchers.IO) { job(it) }
        awaitClose { }
      }.collect {
        flow.value = it
      }
    }
  }
  return Task(flow) { picker.launch(input) }
}