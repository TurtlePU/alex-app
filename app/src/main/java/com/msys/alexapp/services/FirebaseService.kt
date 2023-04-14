package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.AlexAppService
import com.msys.alexapp.data.Role
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

object FirebaseService : AlexAppService {
  override suspend fun signIn(email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
  }

  override fun invitationsFrom(role: Role) =
    FirebaseDatabase.getInstance()
      .getReference("invitations")
      .child(FirebaseAuth.getInstance().currentUser!!.email!!)
      .snapshots.map { data ->
        data.children.filter { it.value == role.toString() }.map { it.key!! }
      }

  override fun juryService(stageID: String) = FirebaseJuryService(stageID)
  override fun stagePreparationService(adminID: String) = FirebaseStagePreparationService(adminID)
  override fun stageService(adminID: String) = FirebaseStageService(adminID)
}