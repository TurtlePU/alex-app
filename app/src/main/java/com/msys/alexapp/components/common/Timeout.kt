package com.msys.alexapp.components.common

import android.icu.util.Calendar
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.ui.theme.AlexAppTheme
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

fun currentDate(): Date = Calendar.getInstance().time
operator fun Date.plus(duration: Duration): Date = Date(time + duration.inWholeMilliseconds)
operator fun Date.minus(other: Date): Duration = (time - other.time).milliseconds
val defaultTimeout: Duration = 5.minutes

@Composable
fun Timeout(progress: Float) {
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
      .height(10.dp)
      .then(modifier),
    color = color,
  )
}

@Preview(showBackground = true)
@Composable
fun ShiningProgressPreview() {
  AlexAppTheme { Timeout(1f) }
}