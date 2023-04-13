package com.msys.alexapp.components

import android.icu.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun Performance.View(
  index: Long,
  deadline: Date,
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  Scaffold(
    floatingActionButton = floatingActionButton,
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Timeout(deadline)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        Text(text = category?.firstWord ?: "")
        Column {
          Text(text = nomination?.firstWord ?: "")
          Text(text = age.toString())
        }
        Column {
          Text(
            text = "$name (#${index + 1})",
            textAlign = TextAlign.Center,
          )
          Text(
            text = performance,
            textAlign = TextAlign.Center
          )
        }
        Text(
          text = city ?: "",
          textAlign = TextAlign.Right
        )
      }
      content()
    }
  }
}

@Composable
fun Timeout(deadline: Date) {
  var time by remember { mutableStateOf(currentDate()) }
  LaunchedEffect(true) {
    while (true) {
      time = currentDate()
      delay(1000.milliseconds)
    }
  }
  val progress = (1f * (deadline.time - time.time) / timeout.inWholeMilliseconds).coerceIn(0f, 1f)
  LinearProgressIndicator(
    progress = progress,
    modifier = Modifier.fillMaxWidth(),
  )
}

fun currentDate(): Date = Calendar.getInstance().time

val timeout: Duration = 5.minutes

val String.firstWord: String get() = this.trim().split(' ').first()

val example = Performance(
  id = "0",
  name = "Android",
  city = "New York",
  category = "II",
  performance = "Sing Along",
  age = 13,
  nomination = "song"
)

@Preview(showBackground = true)
@Composable
fun PerformancePreview() {
  example.View(index = 0, deadline = Date(currentDate().time + timeout.inWholeMilliseconds)) {}
}