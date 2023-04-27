package com.msys.alexapp.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.msys.alexapp.R
import kotlinx.coroutines.launch

data class Commitment(val canCommit: Boolean, val onCommit: suspend () -> Unit)

@Composable
fun HiddenForm(
  commitDescription: String? = null,
  content: @Composable RowScope.() -> Commitment,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    var draft by rememberSaveable { mutableStateOf(false) }
    if (draft) {
      var commitment = Commitment(false) { }
      Row { commitment = content() }
      Row {
        val scope = rememberCoroutineScope()
        Button(
          onClick = { scope.launch { commitment.onCommit(); draft = false } },
          enabled = commitment.canCommit
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = commitDescription,
          )
        }
        Button(onClick = { draft = false }) {
          Icon(
            imageVector = Icons.Filled.Cancel,
            contentDescription = stringResource(R.string.cancel),
          )
        }
      }
    } else {
      Button(
        onClick = { draft = true },
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = commitDescription,
        )
      }
    }
  }
}