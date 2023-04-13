package com.msys.alexapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msys.alexapp.data.Performance

@Composable
fun Performance.View(content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceEvenly,
  ) {
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
          text = performance,
          textAlign = TextAlign.Center
        )
      }
      Text(
        text = city ?: "",
        textAlign = TextAlign.Right
      )
    }
    content()
  }
}

val String.firstWord: String get() = this.trim().split(' ').first()