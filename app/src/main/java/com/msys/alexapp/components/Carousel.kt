package com.msys.alexapp.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.services.Performance
import com.msys.alexapp.services.PerformanceRepo
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.seconds

@Composable
fun Carousel(userID: String) {
  val pageKeys by PerformanceRepo.listFlow.collectAsState(initial = listOf())
  PerformancePager(
    pageKeys = pageKeys,
    performances = { PerformanceRepo[it] },
    ratings = { PerformanceRepo.getRating(it, userID) },
    comments = { PerformanceRepo.getComment(it, userID) },
    rate = { performanceID, rating -> PerformanceRepo.rate(performanceID, userID, rating) },
    comment = { performanceID, comment -> PerformanceRepo.comment(performanceID, userID, comment) },
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerformancePager(
  pageKeys: List<String>,
  performances: (String) -> Flow<Performance>,
  ratings: (String) -> Flow<Double?>,
  comments: (String) -> Flow<String?>,
  rate: suspend (String, Double) -> Unit,
  comment: suspend (String, String) -> Unit,
) {
  HorizontalPager(
    pageCount = pageKeys.size,
    verticalAlignment = Alignment.Top,
    key = { pageKeys[it] },
  ) { index ->
    val id = pageKeys[index]
    val performance by performances(id).collectAsState(initial = null)
    PerformancePage(
      index,
      performance,
      ratings(id),
      comments(id),
      { rate(id, it) },
      { comment(id, it) },
    )
  }
}

@Composable
fun PerformancePage(
  index: Int,
  performance: Performance?,
  rateFlow: Flow<Double?>,
  commentFlow: Flow<String?>,
  sendRating: suspend (Double) -> Unit,
  sendComment: suspend (String) -> Unit,
) {
  var rating: Double? by rememberSaveable { mutableStateOf(null) }
  if (rating == null) {
    val oldRating: Double? by rateFlow.collectAsState(initial = null)
    rating = oldRating
  }
  var comment: String? by rememberSaveable { mutableStateOf(null) }
  if (comment == null) {
    val oldComment: String? by commentFlow.collectAsState(initial = null)
    comment = oldComment
  }
  LaunchedEffect(true) {
    while (true) {
      delay(5.seconds)
      rating?.let { sendRating(it) }
      comment?.let { sendComment(it) }
    }
  }
  if (performance != null) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
    ) {
      performance.View(index)
      RatingPad(rating) { rating = it }
      TextField(
        value = comment ?: "",
        onValueChange = { comment = it },
        modifier = Modifier
          .padding(vertical = 5.dp)
          .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
      )
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
fun Performance.View(index: Int) {
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
        text = performance ?: "",
        textAlign = TextAlign.Center
      )
    }
    Text(
      text = city ?: "",
      textAlign = TextAlign.Right
    )
  }
}

val String.firstWord: String get() = this.trim().split(' ').first()

private val example = Performance(
  "Android",
  "New York",
  "II",
  "Sing Along",
  13,
  "song"
)

@Preview(showBackground = true)
@Composable
fun PerformancePagePreview() {
  AlexAppTheme {
    PerformancePage(
      index = 0,
      performance = example,
      rateFlow = emptyFlow(),
      commentFlow = emptyFlow(),
      sendRating = {},
      sendComment = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PerformancePagerPreview() {
  val examples = mapOf("gibberish" to example)
  PerformancePager(
    pageKeys = examples.keys.toList(),
    performances = { flowOf(examples[it]!!) },
    ratings = { emptyFlow() },
    comments = { emptyFlow() },
    rate = { _, _ -> },
    comment = { _, _ -> },
  )
}