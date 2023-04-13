package com.msys.alexapp.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class Performance(
  val name: String?,
  val city: String?,
  val category: String?,
  val performance: String?,
  val age: Long?,
  val nomination: String?,
)

object PerformanceRepo {
  private val userID: String get() = FirebaseAuth.getInstance().uid!!
  private val performances: DatabaseReference
    get() = FirebaseDatabase.getInstance().getReference("performances")

  private fun child(performanceId: String): DatabaseReference = performances.child(performanceId)

  private fun rating(performanceId: String): DatabaseReference =
    child(performanceId).child("ratings/$userID")

  private fun comment(performanceId: String): DatabaseReference =
    child(performanceId).child("comments/$userID")

  private operator fun DataSnapshot.get(key: String): String? = child(key).getValue<String>()

  val listFlow: Flow<List<String>> =
    performances.snapshots.map { it.children.map { child -> child.key!! } }

  operator fun get(performanceId: String): Flow<Performance> = child(performanceId).snapshots.map {
    Performance(
      name = it["name"],
      city = it["city"],
      category = it["category"],
      performance = it["performance"],
      age = it.child("age").getValue<Long>(),
      nomination = it["nomination"],
    )
  }

  fun getRating(performanceId: String): Flow<Double?> =
    rating(performanceId).snapshots.map { it.getValue<Double>() }

  suspend fun rate(performanceId: String, rating: Double) {
    rating(performanceId).setValue(rating).await()
  }

  fun getComment(performanceId: String): Flow<String?> =
    comment(performanceId).snapshots.map { it.getValue<String>() }

  suspend fun comment(performanceId: String, comment: String) {
    comment(performanceId).setValue(comment).await()
  }
}