package com.msys.alexapp.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.msys.alexapp.data.Role
import com.msys.alexapp.data.Role.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface InvitationService {
  val role: Role
  fun invitationsFrom(role: Role): Flow<List<String>>
}

fun inviter(role: Role): Role? = when (role) {
  ADMIN -> null
  STAGE -> ADMIN
  JURY -> STAGE
}

@Composable
fun InvitationService.Invitations() {
  val invitations by invitationsFrom(inviter(role)!!).collectAsState(initial = listOf())
  LazyColumn { items(invitations) { id -> Text(text = id) } }
}

@Preview
@Composable
fun InvitationsPreview(role: Role = JURY) {
  object : InvitationService {
    override val role: Role get() = role
    override fun invitationsFrom(role: Role): Flow<List<String>> = flowOf(listOf("wowwowowo"))
  }.Invitations()
}