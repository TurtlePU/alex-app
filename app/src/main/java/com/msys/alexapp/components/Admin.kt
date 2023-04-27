package com.msys.alexapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface AdminService {
  val contactsFlow: Flow<Map<String, String>>
  val currentStageFlow: Flow<String>
  suspend fun setCanComment(canComment: Boolean)
  suspend fun addContact(email: String, nickname: String)
  suspend fun deleteContact(email: String)
  suspend fun setStage(email: String)
}

@Composable
fun TaskButton(options: Task, text: String) {
  val progress by options.progressFlow.collectAsStateWithLifecycle(initialValue = 0f)
  Button(onClick = options.start) {
    Column {
      Text(text = text)
      LinearProgressIndicator(progress = progress.coerceIn(0f, 1f))
    }
  }
}

@Composable
fun AdminService.Admin(
  loadParticipants: @Composable () -> Task,
  saveResults: @Composable () -> Task,
) {
  val scope = rememberCoroutineScope()
  Scaffold(
    bottomBar = {
      Row {
        TaskButton(
          options = loadParticipants(),
          text = stringResource(R.string.load_participants)
        )
        Row {
          var canComment by rememberSaveable { mutableStateOf(false) }
          Checkbox(
            checked = canComment,
            onCheckedChange = {
              canComment = !canComment
              scope.launch { setCanComment(canComment) }
            },
          )
          Text(text = stringResource(R.string.comments))
        }
        TaskButton(
          options = saveResults(),
          text = stringResource(R.string.save_results),
        )
      }
    }
  ) { padding ->
    val contacts by contactsFlow.collectAsStateWithLifecycle(initialValue = mapOf())
    val currentStage by currentStageFlow.collectAsStateWithLifecycle(initialValue = null)
    Column(modifier = Modifier.padding(padding)) {
      for ((email, nickname) in contacts) {
        val bg =
          if (email == currentStage) Modifier.background(MaterialTheme.colorScheme.secondary)
          else Modifier
        Row(modifier = bg) {
          Row(modifier = Modifier.clickable { scope.launch { setStage(email) } }) {
            Text(text = email)
            Text(text = nickname)
          }
          Button(onClick = { scope.launch { deleteContact(email) } }) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = stringResource(R.string.delete_contact),
            )
          }
        }
      }
      HiddenForm(commitDescription = stringResource(R.string.add_contact)) {
        var email: String? by rememberSaveable { mutableStateOf(null) }
        var nickname: String? by rememberSaveable { mutableStateOf(null) }
        TextField(
          value = email ?: "",
          onValueChange = { email = it },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
          )
        )
        TextField(
          value = nickname ?: "",
          onValueChange = { nickname = it },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        Commitment(email != null && nickname != null) {
          addContact(email!!, nickname!!)
        }
      }
    }
  }
}