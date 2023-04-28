package com.msys.alexapp.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun Performance.View(
  deadline: Date,
  cornerButton: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  var progress by rememberSaveable { mutableStateOf(0f) }
  LaunchedEffect(true) {
    withContext(Dispatchers.IO) {
      val timeout = deadline - currentDate()
      while (true) {
        val time = currentDate()
        progress = (1 - (deadline - time) / timeout).coerceIn(.0, .1).toFloat()
        delay(1.seconds)
      }
    }
  }
  Scaffold(
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = category?.firstWord ?: "",
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.headlineLarge,
        )
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = nomination?.firstWord ?: "",
            style = MaterialTheme.typography.displayMedium,
          )
          Text(
            text = age.toString(),
            style = MaterialTheme.typography.displayMedium,
          )
        }
        Column(
          modifier = Modifier.weight(3f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "(#$id) $name",
            maxLines = 1,
            style = MaterialTheme.typography.displayMedium,
          )
          Text(
            text = performance,
            maxLines = 1,
            style = MaterialTheme.typography.displayMedium,
          )
        }
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          cornerButton()
          Text(
            text = city ?: "",
            style = MaterialTheme.typography.displayMedium,
          )
        }
      }
    },
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    ) {
      Timeout(progress)
      content()
    }
  }
}

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

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun PerformancePreview() {
  AlexAppTheme { example.View(currentDate()) {} }
}