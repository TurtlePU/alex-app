package com.msys.alexapp.components.stage.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.msys.alexapp.data.Performance

@Composable
fun Performance.StagingCard(
  isStaged: (String) -> Boolean,
  addStaged: (String) -> Unit,
  removeStaged: (String) -> Unit,
) {
  val cardModifier =
    if (isStaged(id)) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    else Modifier
  Card(
    modifier = Modifier
      .clickable { if (isStaged(id)) removeStaged(id) else addStaged(id) }
      .then(cardModifier),
  )
}

val longExample = anonymousPerformance(0,
  "Android, Safari, Example, Talky and others present",
  "Sing along with another performers all around the world"
)

@Preview(showBackground = true)
@Composable
fun StagingCardPreview() {
  var isStaged by rememberSaveable { mutableStateOf(false) }
  longExample.StagingCard(
    isStaged = { isStaged },
    addStaged = { isStaged = true },
    removeStaged = { isStaged = false },
  )
}