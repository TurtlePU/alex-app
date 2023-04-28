package com.msys.alexapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.common.View
import com.msys.alexapp.components.common.currentDate
import com.msys.alexapp.components.common.example
import com.msys.alexapp.components.common.progressFlow
import com.msys.alexapp.data.JuryReport
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

data class Advice(
  val deadline: Date,
  val canComment: Boolean = false,
)

interface JuryService {
  val currentPerformance: Flow<Performance?>
  val juryAdvice: Flow<Advice>
  fun isEvaluated(id: String): Flow<Boolean>
  fun averageRating(id: String): Flow<Double?>
  suspend fun sendInvitation()
  suspend fun evaluate(id: String, report: JuryReport)
}

@Composable
fun JuryService.Carousel(exit: () -> Unit) {
  val performance by currentPerformance.collectAsStateWithLifecycle(initialValue = null)
  val view = performance
  LaunchedEffect(Unit) {
    sendInvitation()
  }
  view?.let {
    val hidePage by isEvaluated(it.id).collectAsStateWithLifecycle(initialValue = true)
    if (hidePage) {
      val rating by averageRating(it.id).collectAsStateWithLifecycle(initialValue = .0)
      RatingPage(rating ?: Double.NaN)
    } else {
      val advice by juryAdvice.collectAsStateWithLifecycle(initialValue = Advice(currentDate()))
      advice.PerformancePage(it) { report -> evaluate(it.id, report) }
    }
  } ?: BreakPage(exit)
}

@Composable
fun RatingPage(rating: Double) {
  Text(text = stringResource(R.string.rated_performance, rating))
}

@Composable
fun BreakPage(exit: () -> Unit) {
  Column {
    Text(text = stringResource(R.string.no_performance))
    Button(onClick = exit) {
      Icon(
        imageVector = Icons.Filled.ExitToApp,
        contentDescription = stringResource(R.string.back_to_auth)
      )
    }
  }
}

@Composable
fun Advice.PerformancePage(
  performance: Performance,
  evaluate: suspend (JuryReport) -> Unit
) {
  var rating: Double? by rememberSaveable { mutableStateOf(null) }
  var comment: String? by rememberSaveable { mutableStateOf(null) }
  val scope = rememberCoroutineScope()
  performance.View(
    progress = progressFlow(deadline),
    floatingActionButton = {
      if (rating != null) {
        FloatingActionButton(
          onClick = { scope.launch { evaluate(JuryReport(rating!!, comment)) } },
        ) {
          Icon(
            imageVector = Icons.Filled.StarRate,
            contentDescription = stringResource(R.string.rate_button),
          )
        }
      }
    }
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      RatingPad(rating) { rating = it }
      if (canComment) {
        CommentSection(comment ?: "") { comment = it }
      }
    }
  }
}

@Composable
fun ColumnScope.RatingPad(rating: Double?, setRating: (Double) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    for (i in 5..9) {
      RatingButton(
        i.toDouble(), rating, setRating,
        Modifier
          .fillMaxHeight()
          .weight(1f),
      )
    }
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    for (i in 5..9) {
      RatingButton(
        i + 0.5, rating, setRating,
        Modifier
          .fillMaxHeight()
          .weight(1f),
      )
    }
  }
  RatingButton(
    10.0, rating, setRating,
    Modifier
      .fillMaxWidth()
      .weight(1f)
  )
}

@Composable
fun RatingButton(
  newRating: Double,
  oldRating: Double?,
  rate: (Double) -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = { rate(newRating) },
    modifier = modifier,
    enabled = newRating != oldRating,
    shape = RectangleShape,
  ) {
    Text(
      text = newRating.toString(),
      style = MaterialTheme.typography.displayLarge,
    )
  }
}

@Composable
fun ColumnScope.CommentSection(value: String, onValueChange: (String) -> Unit) {
  TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = Modifier
      .fillMaxWidth()
      .weight(2f),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
  )
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun PagePreview() {
  val advice = Advice(deadline = currentDate(), canComment = true)
  AlexAppTheme { advice.PerformancePage(example) { } }
}

@Preview
@Composable
fun EvaluatedPreview() {
  AlexAppTheme { RatingPage(rating = 5.5) }
}