package com.msys.alexapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Advice
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface JuryService {
  val currentPerformance: Flow<Performance?>
  val juryAdvice: Flow<Advice>
  fun isEvaluated(id: String): Flow<Boolean>
  fun averageRating(id: String): Flow<Double?>
  suspend fun sendInvitation()
  suspend fun evaluate(id: String, rating: Double, comment: String?)
}

@Composable
fun JuryService.Carousel() {
  LaunchedEffect(true) {
    sendInvitation()
  }
  val performance by currentPerformance.collectAsStateWithLifecycle(initialValue = null)
  performance?.let {
    val hidePage by isEvaluated(it.id).collectAsStateWithLifecycle(initialValue = true)
    if (hidePage) {
      val rating by averageRating(it.id).collectAsStateWithLifecycle(initialValue = .0)
      RatingPage(rating!!)
    } else {
      val advice by juryAdvice.collectAsStateWithLifecycle(initialValue = Advice(currentDate()))
      advice.PerformancePage(it) { rating, comment ->
        evaluate(it.id, rating, comment)
      }
    }
  } ?: Text(text = stringResource(R.string.no_performance))
}

@Composable
fun RatingPage(rating: Double) {
  Text(text = stringResource(R.string.rated_performance, rating))
}

@Composable
fun Advice.PerformancePage(
  performance: Performance,
  evaluate: suspend (Double, String?) -> Unit
) {
  var rating: Double? by rememberSaveable { mutableStateOf(null) }
  var comment: String? by rememberSaveable { mutableStateOf(null) }
  val scope = rememberCoroutineScope()
  performance.View(
    index = index,
    deadline = deadline,
    floatingActionButton = {
      if (rating != null) {
        FloatingActionButton(
          onClick = { scope.launch { evaluate(rating!!, comment) } },
        ) {
          Icon(
            imageVector = Icons.Filled.StarRate,
            contentDescription = stringResource(R.string.rate_button),
          )
        }
      }
    }
  ) {
    RatingPad(rating) { rating = it }
    if (canComment) {
      CommentSection(comment ?: "") { comment = it }
    }
  }
}

@Composable
fun RatingPad(rating: Double?, setRating: (Double) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    for (i in 5..9) {
      RatingButton(i.toDouble(), rating, setRating)
    }
  }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    for (i in 5..9) {
      RatingButton(i + 0.5, rating, setRating)
    }
  }
  RatingButton(
    10.0, rating, setRating,
    Modifier
      .fillMaxWidth()
      .padding(5.dp)
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
  ) {
    Text(text = newRating.toString())
  }
}

@Composable
fun CommentSection(value: String, onValueChange: (String) -> Unit) {
  TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = Modifier
      .padding(vertical = 5.dp)
      .fillMaxWidth(),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
  )
}

@Preview(showBackground = true)
@Composable
fun PagePreview(advice: Advice = Advice(currentDate())) {
  AlexAppTheme { advice.PerformancePage(example) { _, _ -> } }
}

@Preview
@Composable
fun EvaluatedPreview() {
  AlexAppTheme { RatingPage(rating = 5.5) }
}