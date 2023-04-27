package com.msys.alexapp.components.admin

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.msys.alexapp.R
import com.msys.alexapp.components.common.Commitment
import com.msys.alexapp.components.common.HiddenForm

@Composable
fun NewContact(addContact: suspend (String, String) -> Unit) {
  HiddenForm(commitDescription = stringResource(R.string.add_contact)) {
    var email: String? by rememberSaveable { mutableStateOf(null) }
    var nickname: String? by rememberSaveable { mutableStateOf(null) }
    Row(modifier = Modifier.fillMaxWidth()) {
      OutlinedTextField(
        value = email ?: "",
        onValueChange = { email = it },
        modifier = Modifier.weight(1f),
        textStyle = TextStyle(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.None,
          autoCorrect = false,
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next,
        )
      )
      OutlinedTextField(
        value = nickname ?: "",
        onValueChange = { nickname = it },
        modifier = Modifier.weight(1f),
        textStyle = TextStyle(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      )
    }
    Commitment(email != null && nickname != null) {
      addContact(email!!, nickname!!)
    }
  }
}