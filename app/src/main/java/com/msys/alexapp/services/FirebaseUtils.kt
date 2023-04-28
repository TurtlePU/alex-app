package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

val currentUID: String get() = FirebaseAuth.getInstance().uid!!
val currentEmail: String get() = FirebaseAuth.getInstance().currentUser!!.email!!
val data: DatabaseReference get() = FirebaseDatabase.getInstance().getReference("data")

suspend fun DatabaseReference.chooseFriends(myRole: Role, emails: Map<String, String>) {
  val friendTask = child("friends").setValue(emails)
  emails
    .map { email ->
      FirebaseDatabase.getInstance()
        .getReference("invitations/${email.key}/${key!!}")
        .setValue(myRole.toString())
        .asDeferred()
    }
    .awaitAll()
  friendTask.await()
}

val DatabaseReference.performances: Flow<List<Performance>>
  get() = child("performances").snapshots.map {
    it.children.map(DataSnapshot::asPerformance)
  }

val DataSnapshot.asPerformance: Performance
  get() = Performance(
    id = key!!,
    name = child("name").getValue<String>()!!,
    performance = child("performance").getValue<String>()!!,
    city = child("city").getValue<String>(),
    category = child("category").getValue<String>(),
    age = child("age").getValue<Long>(),
    nomination = child("nomination").getValue<String>(),
  )