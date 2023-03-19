package com.msys.alexapp.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.msys.alexapp.services.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun Carousel(userID: String) {
  val pageCount by performanceCountFlow.collectAsState(initial = 0)
  PerformancePager(
    pageCount = pageCount,
    performances = ::performanceFlow,
    rate = { index, rating -> rate(index, userID, rating) },
    comment = { index, comment -> comment(index, userID, comment) }
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
  HorizontalPager(pageCount = pageCount) { index ->
    val performance by performances(index).collectAsState(initial = null)
    PerformancePage(performance, { rate(index, it) }, { comment(index, it) })
  }
}

@Composable
fun PerformancePage(
  performance: Performance?,
  rate: suspend (Double) -> Unit,
  comment: suspend (String) -> Unit
) {
  TODO("Not yet implemented")
}

@Preview(showBackground = true)
@Composable
fun PerformancePagerPreview() {
  val examples = listOf(Performance("Android"))
  PerformancePager(
    pageCount = examples.size,
    performances = { flowOf(examples[it]) },
    rate = { _, _ -> },
    comment = { _, _ -> },
  )
}