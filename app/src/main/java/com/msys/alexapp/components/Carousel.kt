package com.msys.alexapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface CarouselService {
  val currentPerformance: Flow<Performance>
  val canComment: Flow<Boolean>
  fun isEvaluated(id: String): Flow<Boolean>
  suspend fun sendInvitation()
  suspend fun evaluate(id: String, rating: Double, comment: String?)
}

@Composable
fun CarouselService.Carousel() {
  LaunchedEffect(true) {
    sendInvitation()
  }
  val performance by currentPerformance.collectAsState(initial = null)
  performance?.let {
    val hidePage by isEvaluated(it.id).collectAsState(initial = true)
    if (hidePage) {
      Text(text = stringResource(R.string.rated_performance))
    } else {
      val canComment by this.canComment.collectAsState(initial = false)
      PerformancePage(it, canComment) { rating, comment ->
        evaluate(it.id, rating, comment)
      }
    }
  } ?: Text(text = stringResource(R.string.no_performance))
}

@Composable
fun PerformancePage(
  performance: Performance,
  canComment: Boolean,
  evaluate: suspend (Double, String?) -> Unit
) {
  var rating: Double? by rememberSaveable { mutableStateOf(null) }
  val scope = rememberCoroutineScope()
  val report: (String?) -> Unit = { comment ->
    rating?.let { rating -> scope.launch { evaluate(rating, comment) } }
  }
  performance.View {
    RatingPad(rating) { rating = it }
    if (canComment) {
      CommentSection(report)
    } else {
      OutlinedButton(onClick = { report(null) }) {
        Text(text = stringResource(R.string.rate_button))
      }
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
fun CommentSection(sendComment: (String?) -> Unit) {
  var comment: String? by rememberSaveable { mutableStateOf(null) }
  TextField(
    value = comment ?: "",
    onValueChange = { comment = it },
    modifier = Modifier
      .padding(vertical = 5.dp)
      .fillMaxWidth(),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { sendComment(comment) }),
  )
}

private val example = Performance(
  id = "0",
  index = 0,
  name = "Android",
  city = "New York",
  category = "II",
  performance = "Sing Along",
  age = 13,
  nomination = "song"
)

@Preview(showBackground = true)
@Composable
fun PagePreview(canComment: Boolean = true) {
  AlexAppTheme {
    PerformancePage(
      performance = example,
      canComment = canComment,
    ) { _, _ -> }
  }
}