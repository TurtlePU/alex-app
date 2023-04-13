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

private fun invitations(email: String, uid: String? = null): DatabaseReference =
  FirebaseDatabase.getInstance()
    .getReference("invitations/$email")
    .run { uid?.let { child(it) } ?: this }

private fun friends(uid: String): DatabaseReference =
  FirebaseDatabase.getInstance().getReference("data/$uid/friends")

fun friendIdsByRole(role: String): Flow<List<String>> =
  invitations(FirebaseAuth.getInstance().currentUser!!.email!!)
    .snapshots.map { data -> data.children.filter { it.value == role }.map { it.key!! } }

suspend fun chooseFriends(myRole: String, emails: List<String>) {
  val uid = FirebaseAuth.getInstance().uid!!
  val friendTask = friends(uid).setValue(emails.associateWith { "" })
  emails.map { email -> invitations(email, uid).setValue(myRole).asDeferred() }.awaitAll()
  friendTask.await()
}