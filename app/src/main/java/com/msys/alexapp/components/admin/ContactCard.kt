package com.msys.alexapp.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.msys.alexapp.R

@Composable
fun Map.Entry<String, String>.ContactCard(
  isStage: (String) -> Boolean,
  setStage: (String) -> Unit,
  deleteContact: (String) -> Unit,
) {
  val bg =
    if (isStage(key)) Modifier.background(MaterialTheme.colorScheme.secondary)
    else Modifier
  Row(modifier = bg.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .clickable { setStage(key) }
        .fillMaxWidth()
    ) {
      Text(text = key, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
      Text(text = value, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
    Button(onClick = { deleteContact(key) }) {
      Icon(
        imageVector = Icons.Filled.Delete,
        contentDescription = stringResource(R.string.delete_contact),
      )
    }
  }
}