package com.msys.alexapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.admin.ContactCard
import com.msys.alexapp.components.admin.NewContact
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
fun AdminService.Admin(
  loadParticipants: @Composable () -> Task,
  saveResults: @Composable () -> Task,
) {
  val scope = rememberCoroutineScope()
  Scaffold(
    bottomBar = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        TaskButton(
          options = loadParticipants(),
          text = stringResource(R.string.load_participants)
        )
        CommentBox { scope.launch { setCanComment(it) } }
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
      for (entry in contacts) {
        entry.ContactCard(
          isStage = { currentStage == it },
          setStage = { scope.launch { setStage(it) } },
          deleteContact = { scope.launch { deleteContact(it) } },
        )
      }
      NewContact(::addContact)
    }
  }
}

@Composable
fun CommentBox(setCanComment: (Boolean) -> Unit) {
  Row {
    var canComment by rememberSaveable { mutableStateOf(false) }
    Checkbox(
      checked = canComment,
      onCheckedChange = {
        canComment = !canComment
        setCanComment(canComment)
      },
    )
    Text(text = stringResource(R.string.comments))
  }
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