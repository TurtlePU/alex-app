package com.msys.alexapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.components.admin.Card
import com.msys.alexapp.components.admin.CommentBox
import com.msys.alexapp.components.admin.Contact
import com.msys.alexapp.components.admin.NewContact
import com.msys.alexapp.tasks.Task
import com.msys.alexapp.tasks.TaskButton
import com.msys.alexapp.tasks.dummyTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface AdminService {
  val contactsFlow: Flow<List<Contact>>
  val currentStageFlow: Flow<String>
  suspend fun sendDegrees(degrees: Map<Double, String>)
  suspend fun setCanComment(canComment: Boolean)
  suspend fun addContact(email: String, nickname: String)
  suspend fun deleteContact(email: String)
  suspend fun setStage(email: String)

  interface Dummy : AdminService {
    val contacts: SnapshotStateMap<String, String>
    override val contactsFlow: Flow<List<Contact>>
      get() = snapshotFlow { contacts }
        .map { it.map { (email, nickname) -> Contact(email, nickname) } }

    override val currentStageFlow: MutableStateFlow<String>

    override suspend fun sendDegrees(degrees: Map<Double, String>) {}

    override suspend fun setCanComment(canComment: Boolean) {}

    override suspend fun addContact(email: String, nickname: String) {
      contacts[email] = nickname
    }

    override suspend fun deleteContact(email: String) {
      contacts.remove(email)
    }

    override suspend fun setStage(email: String) {
      currentStageFlow.value = email
    }
  }
}

@Composable
fun AdminService.Admin(
  loadParticipants: @Composable () -> Task,
  saveResults: @Composable () -> Task,
) {
  LaunchedEffect(true) {
    sendDegrees(mapOf(
      10.0 to "Л1", 9.49 to "Л2", 8.99 to "Л3",
      7.99 to "Д1", 6.99 to "Д2", 5.99 to "Д3",
      5.0 to "УЧ",
    ))
  }
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
    val contacts by contactsFlow.collectAsStateWithLifecycle(initialValue = listOf())
    val currentStage by currentStageFlow.collectAsStateWithLifecycle(initialValue = null)
    Column(modifier = Modifier.padding(padding)) {
      for (contact in contacts) {
        contact.Card(
          isStage = { currentStage == it },
          setStage = { scope.launch { setStage(it) } },
          deleteContact = { scope.launch { deleteContact(it) } },
        )
      }
      NewContact(::addContact)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AdminPreview() {
  object : AdminService.Dummy {
    override val contacts: SnapshotStateMap<String, String> = remember { mutableStateMapOf() }
    override val currentStageFlow: MutableStateFlow<String> = MutableStateFlow("")
  }.Admin(
    loadParticipants = { dummyTask() },
    saveResults = { dummyTask() },
  )
}