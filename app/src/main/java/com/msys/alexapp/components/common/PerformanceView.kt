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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
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
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
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
        Text(
          text = category?.firstWord ?: "",
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
        )
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(text = nomination?.firstWord ?: "", maxLines = 1)
          Text(text = age?.let { stringResource(R.string.age_template, it) } ?: "")
        }
        Column(
          modifier = Modifier.weight(6f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "(#$id) $name",
            textAlign = TextAlign.Center,
            maxLines = 1,
          )
          Text(text = performance, maxLines = 1)
        }
        Text(
          text = city ?: "",
          modifier = Modifier.weight(2f),
          textAlign = TextAlign.Center,
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
      verticalArrangement = verticalArrangement,
      horizontalAlignment = horizontalAlignment,
    ) {
      Timeout(deadline)
      content()
    }
  }
}

fun currentDate(): Date = Calendar.getInstance().time
operator fun Date.plus(duration: Duration): Date = Date(time + duration.inWholeMilliseconds)
operator fun Date.minus(other: Date): Duration = (time - other.time).milliseconds
val defaultTimeout: Duration = 5.minutes

@Composable
fun Timeout(deadline: Date) {
  var time by remember { mutableStateOf(currentDate()) }
  LaunchedEffect(true) {
    while (true) {
      time = currentDate()
      delay(1000.milliseconds)
    }
  }
  val progress = (1 - (deadline - time) / defaultTimeout).coerceIn(0.0, 1.0).toFloat()
  val infiniteTransition = rememberInfiniteTransition()
  val animatedElevation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(
      animation = tween(easing = FastOutLinearInEasing),
      repeatMode = RepeatMode.Reverse,
    )
  )
  val (modifier, color) = if (progress == 1f) {
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

val String.firstWord: String get() = this.trim().split(' ').first()

val example = Performance(
  id = "0",
  name = "Android, Kelly Donovan, Winners of the stage and others, including you, your mom",
  city = "New York",
  category = "II",
  performance = "Sing Along",
  age = 13,
  nomination = "song"
)

@Preview(showBackground = true, device = "id:Nexus 10")
@Composable
fun PerformancePreview() {
  AlexAppTheme {
    example.View(deadline = currentDate()) {}
  }
}