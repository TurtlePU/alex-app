package com.msys.alexapp.components.stage.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msys.alexapp.data.Performance

@Composable
fun Performance.Card(modifier: Modifier = Modifier) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
      .padding(10.dp),
    horizontalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Text(text = id)
    Text(
      text = name,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
    )
    Text(
      text = performance,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
    )
  }
}