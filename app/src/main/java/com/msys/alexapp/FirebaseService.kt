package com.msys.alexapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.AlexAppService
import com.msys.alexapp.data.Role
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

object FirebaseService : AlexAppService {
  private fun friends(uid: String): DatabaseReference =
    FirebaseDatabase.getInstance().getReference("data/$uid/friends")

  private fun invitations(email: String, uid: String? = null): DatabaseReference =
    FirebaseDatabase.getInstance()
      .getReference("invitations/$email")
      .run { uid?.let { child(it) } ?: this }

  override suspend fun signIn(email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
  }

  override fun invitationsFrom(role: Role) =
    invitations(FirebaseAuth.getInstance().currentUser!!.email!!)
      .snapshots.map { data ->
        data.children.filter { it.value == role.toString() }.map { it.key!! }
      }

  suspend fun chooseFriends(myRole: Role, emails: List<String>) {
    val uid = FirebaseAuth.getInstance().uid!!
    val friendTask = friends(uid).setValue(emails.associateWith { "" })
    val role = myRole.toString()
    emails.map { email -> invitations(email, uid).setValue(role).asDeferred() }.awaitAll()
    friendTask.await()
  }
}