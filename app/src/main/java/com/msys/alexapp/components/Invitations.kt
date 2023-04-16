package com.msys.alexapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun Invitations(invitationsFlow: Flow<List<String>>, reportInvitation: (String) -> Unit) {
  val invitations by invitationsFlow.collectAsStateWithLifecycle(initialValue = listOf())
  if (invitations.isEmpty()) {
    BigBox { CircularProgressIndicator() }
  } else if (invitations.size == 1) {
    LaunchedEffect(invitations) {
      reportInvitation(invitations[0])
    }
  } else {
    InvitationsPicker(invitations, reportInvitation)
  }
}

@Composable
fun InvitationsPicker(invitations: List<String>, reportInvitation: (String) -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceEvenly,
  ) {
    for (id in invitations) {
      BigBox(
        modifier = Modifier
          .weight(1f)
          .clickable { reportInvitation(id) }
      ) {
        Text(text = id)
      }
    }
  }
}

@Composable
fun BigBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentAlignment = Alignment.Center,
    content = content,
  )
}

@Preview
@Composable
fun InvitationsPreview() {
  InvitationsPicker(listOf("wowowowow")) {}
}