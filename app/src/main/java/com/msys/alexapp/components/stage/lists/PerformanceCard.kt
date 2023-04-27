package com.msys.alexapp.components.stage.lists

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.msys.alexapp.data.Performance

@Composable
fun Performance.Card(modifier: Modifier = Modifier) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(text = id)
    Text(text = name)
    Text(text = performance)
  }
}