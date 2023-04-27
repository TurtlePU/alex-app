package com.msys.alexapp.components.stage.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.msys.alexapp.data.Performance

@Composable
fun Performance.StagingCard(
  isStaged: (String) -> Boolean,
  addStaged: (String) -> Unit,
  removeStaged: (String) -> Unit,
) {
  val cardModifier =
    if (isStaged(id)) Modifier.background(MaterialTheme.colorScheme.primary)
    else Modifier
  Card(modifier = cardModifier.clickable {
    if (isStaged(id)) removeStaged(id)
    else addStaged(id)
  })
}