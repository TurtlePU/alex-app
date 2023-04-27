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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.msys.alexapp.R

data class Contact(val email: String, val nickname: String)

@Composable
fun Contact.Card(
  isStage: (String) -> Boolean,
  setStage: (String) -> Unit,
  deleteContact: (String) -> Unit,
) {
  val bg =
    if (isStage(email)) Modifier.background(MaterialTheme.colorScheme.secondary)
    else Modifier
  Row(modifier = bg.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .clickable { setStage(email) }
        .fillMaxWidth()
    ) {
      Text(text = email, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
      Text(text = nickname, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
    Button(onClick = { deleteContact(email) }) {
      Icon(
        imageVector = Icons.Filled.Delete,
        contentDescription = stringResource(R.string.delete_contact),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ContactCardPreview() {
  var isStage by rememberSaveable { mutableStateOf(false) }
  Contact("android@example.com", "Android").Card(
    isStage = { isStage }, setStage = { isStage = !isStage },
  ) { }
}