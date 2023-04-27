package com.msys.alexapp.components.stage.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.StageReport

@Composable
fun Pair<Performance, StageReport>.Card(
  onClick: (String) -> Unit,
  isExpanded: (String) -> Boolean
) {
  Column {
    first.Card(modifier = Modifier.clickable { onClick(first.id) })
    val expanded = isExpanded(first.id)
    val transitionState = remember { MutableTransitionState(expanded) }
    AnimatedVisibility(visibleState = transitionState) { second.Card() }
  }
}

@Composable
fun StageReport.Card() {
  Row(modifier = Modifier.fillMaxWidth()) {
    Text(text = averageRating.toString())
    val commentText = comments.entries.joinToString(separator = "\n") { it.run { "$key: $value" } }
    TextField(
      value = commentText,
      onValueChange = {},
      readOnly = true,
      trailingIcon = {
        val clipboard = LocalClipboardManager.current
        IconButton(onClick = { clipboard.setText(AnnotatedString(commentText)) }) {
          Icon(
            imageVector = Icons.Filled.CopyAll,
            contentDescription = stringResource(R.string.copy_comments),
          )
        }
      },
      singleLine = false,
    )
  }
}