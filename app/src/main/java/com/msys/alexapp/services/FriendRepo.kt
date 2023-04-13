package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

enum class Role {
  ADMIN, STAGE, JURY;

  override fun toString() = when (this) {
    ADMIN -> "admin"
    STAGE -> "stage"
    JURY -> "jury"
  }
}

private fun invitations(email: String, uid: String? = null): DatabaseReference =
  FirebaseDatabase.getInstance()
    .getReference("invitations/$email")
    .run { uid?.let { child(it) } ?: this }

private fun friends(uid: String): DatabaseReference =
  FirebaseDatabase.getInstance().getReference("data/$uid/friends")

fun friendIdsByRole(role: Role): Flow<List<String>> =
  invitations(FirebaseAuth.getInstance().currentUser!!.email!!)
    .snapshots.map { data -> data.children.filter { it.value == role.toString() }.map { it.key!! } }

suspend fun chooseFriends(myRole: Role, emails: List<String>) {
  val uid = FirebaseAuth.getInstance().uid!!
  val friendTask = friends(uid).setValue(emails.associateWith { "" })
  val role = myRole.toString()
  emails.map { email -> invitations(email, uid).setValue(role).asDeferred() }.awaitAll()
  friendTask.await()
}