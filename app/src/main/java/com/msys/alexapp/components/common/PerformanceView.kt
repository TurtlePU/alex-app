package com.msys.alexapp.components.common

import android.icu.util.Calendar
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun Performance.View(
  deadline: Date,
  cornerButton: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  Scaffold(
    topBar = {
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
            text = "$name (#$id)",
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
        cornerButton()
      }
    },
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Timeout(deadline)
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
  val progress = (1 - 1f * (deadline.time - time.time) / timeout.inWholeMilliseconds)
    .coerceIn(0f, 1f)
  val (modifier, color) = if (progress == 1f) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedElevation by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 5f,
      animationSpec = infiniteRepeatable(
        animation = tween(
          easing = FastOutLinearInEasing,
        ),
        repeatMode = RepeatMode.Reverse,
      )
    )
    Modifier.shadow(
      elevation = animatedElevation.dp,
      ambientColor = Color.Red,
    ) to Color.Red
  } else {
    Modifier to ProgressIndicatorDefaults.linearColor
  }
  LinearProgressIndicator(
    progress = progress,
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    color = color,
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
  example.View(deadline = Date(currentDate().time + timeout.inWholeMilliseconds)) {}
}