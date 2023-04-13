package com.msys.alexapp.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun Invitations(uIDs: Flow<List<String>>) {
  val invitations by uIDs.collectAsState(initial = listOf())
  LazyColumn { items(invitations) { id -> Text(text = id) } }
}

@Preview
@Composable
fun InvitationsPreview() {
  Invitations(flowOf(listOf("wowowowow")))
}