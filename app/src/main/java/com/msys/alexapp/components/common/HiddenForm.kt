package com.msys.alexapp.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.msys.alexapp.R
import kotlinx.coroutines.launch

data class Commitment(val canCommit: Boolean, val onCommit: suspend () -> Unit)

@Composable
fun HiddenForm(
  modifier: Modifier = Modifier,
  contentArrangement: Arrangement.Horizontal = Arrangement.Start,
  commitDescription: String? = null,
  content: @Composable RowScope.() -> Commitment,
) {
  var draft by rememberSaveable { mutableStateOf(false) }
  val arrangement = if (draft) contentArrangement else Arrangement.Center
  Row(
    modifier = modifier,
    horizontalArrangement = arrangement,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (draft) {
      val commitment = content()
      Row {
        val scope = rememberCoroutineScope()
        Button(
          onClick = { scope.launch { commitment.onCommit(); draft = false } },
          enabled = commitment.canCommit,
          shape = CircleShape.copy(topEnd = CornerSize(0), bottomEnd = CornerSize(0))
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = commitDescription,
          )
        }
        Button(
          onClick = { draft = false },
          shape = CircleShape.copy(topStart = CornerSize(0), bottomStart = CornerSize(0))
        ) {
          Icon(
            imageVector = Icons.Filled.Cancel,
            contentDescription = stringResource(R.string.cancel),
          )
        }
      }
    } else {
      Button(onClick = { draft = true }) {
        if (commitDescription != null) {
          Text(text = commitDescription)
        } else {
          Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        }
      }
    }
  }
}