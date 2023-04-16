package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.AlexAppService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

object FirebaseService : AlexAppService {
  private val admin get() = data.child(currentUID)

  override suspend fun signIn(email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
  }

  override val contactsFlow: Flow<Map<String, String>>
    get() = admin.child("contacts").snapshots
      .map { it.getValue<Map<String, String>>() ?: mapOf() }
      .map { it.mapKeys { it.key.replace(',', '.') } }

  override suspend fun setCanComment(canComment: Boolean) {
    admin.child("canComment").setValue(canComment).await()
  }

  override suspend fun addPerformance(performance: Performance) {
    admin.child("performances/${performance.id}").setValue(performance.toMap()).await()
  }

  override suspend fun addContact(email: String, nickname: String) {
    admin.child("contacts/${email.replace('.', ',')}").setValue(nickname).await()
  }

  override suspend fun deleteContact(email: String) {
    admin.child("contacts/${email.replace('.', ',')}").removeValue().await()
  }

  override suspend fun setStage(email: String) {
    admin.chooseFriends(Role.ADMIN, mapOf(email.replace('.', ',') to email.replace('.', ',')))
  }

  override fun invitationsFrom(role: Role) =
    FirebaseDatabase.getInstance()
      .getReference("invitations")
      .child(currentEmail.replace('.', ','))
      .snapshots.map { data ->
        data.children.filter { it.value == role.toString() }.map { it.key!! }
      }

  override fun juryService(stageID: String) = FirebaseJuryService(stageID)
  override fun stagePreparationService(adminID: String) = FirebaseStagePreparationService(adminID)
  override fun stageService(adminID: String) = FirebaseStageService(adminID)
}