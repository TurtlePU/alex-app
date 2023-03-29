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
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.seconds

@Composable
fun Carousel(userID: String) {
  val pageCount by PerformanceRepo.countFlow.collectAsState(initial = 0)
  PerformancePager(
    pageCount = pageCount,
    performances = { PerformanceRepo[it] },
    rate = { index, rating -> PerformanceRepo.rate(index, userID, rating) },
    comment = { index, comment -> PerformanceRepo.comment(index, userID, comment) }
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerformancePager(
  pageCount: Int,
  performances: (Int) -> Flow<Performance>,
  rate: suspend (Int, Double) -> Unit,
  comment: suspend (Int, String) -> Unit
) {
  HorizontalPager(
    pageCount = pageCount,
    verticalAlignment = Alignment.Top,
  ) { index ->
    val performance by performances(index).collectAsState(initial = null)
    PerformancePage(performance, { rate(index, it) }, { comment(index, it) })
  }
}

@Composable
fun PerformancePage(
  performance: Performance?,
  sendRating: suspend (Double) -> Unit,
  sendComment: suspend (String) -> Unit,
) {
  var rating: Double? by rememberSaveable { mutableStateOf(null) }
  var comment: String? by rememberSaveable { mutableStateOf(null) }
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
      performance.View()
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
fun Performance.View() {
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
  0,
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
    PerformancePage(performance = example, sendRating = {}, sendComment = {})
  }
}

@Preview(showBackground = true)
@Composable
fun PerformancePagerPreview() {
  val examples = listOf(example)
  PerformancePager(
    pageCount = examples.size,
    performances = { flowOf(examples[it]) },
    rate = { _, _ -> },
    comment = { _, _ -> },
  )
}