package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

val currentUID: String get() = FirebaseAuth.getInstance().uid!!
val data: DatabaseReference get() = FirebaseDatabase.getInstance().getReference("data")

suspend fun DatabaseReference.chooseFriends(myRole: Role, emails: List<String>) {
  val friendTask = child("friends").setValue(emails.associateWith { "" })
  val role = myRole.toString()
  emails
    .map { email ->
      FirebaseDatabase.getInstance()
        .getReference("invitations/$email/${key!!}")
        .setValue(role)
        .asDeferred()
    }
    .awaitAll()
  friendTask.await()
}

fun averageRatingFlow(id: String): Flow<Double?> =
  FirebaseService
    .invitationsFrom(Role.JURY)
    .map { jury ->
      jury
        .map { data.child("$it/$id/rating").get().asDeferred() }
        .awaitAll()
    }
    .map { ratings ->
      ratings
        .mapNotNull { it.getValue<Double>() }
        .run { if (isEmpty()) null else sum() / size }
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