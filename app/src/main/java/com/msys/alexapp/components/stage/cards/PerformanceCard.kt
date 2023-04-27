package com.msys.alexapp.components.stage.cards

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.msys.alexapp.data.Performance

@Composable
fun Performance.Card(modifier: Modifier = Modifier) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(text = id)
    Text(
      text = name,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = performance,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
    )
  }
}