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
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

interface CarouselService {
  val currentPerformance: Flow<Performance?>
  val performanceCount: Flow<Long>
  val canComment: Flow<Boolean>
  val deadline: Flow<Date>
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
      val index by performanceCount.collectAsState(initial = 0)
      val deadline by this.deadline.collectAsState(initial = Date())
      PerformancePage(it, index, deadline, canComment) { rating, comment ->
        evaluate(it.id, rating, comment)
      }
    }
  } ?: Text(text = stringResource(R.string.no_performance))
}

@Composable
fun PerformancePage(
  performance: Performance,
  index: Long,
  deadline: Date,
  canComment: Boolean,
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

private val example = Performance(
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
fun PagePreview(canComment: Boolean = true) {
  AlexAppTheme {
    PerformancePage(
      performance = example,
      index = 0,
      deadline = Date(Date().time + timeout.inWholeMilliseconds),
      canComment = canComment,
    ) { _, _ -> }
  }
}