package com.msys.alexapp.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class Performance(
  val id: Int,
  val name: String?,
  val city: String?,
  val category: String?,
  val performance: String?,
  val age: Long?,
  val nomination: String?,
)

object PerformanceRepo {
  private val performances: DatabaseReference =
    FirebaseDatabase.getInstance().getReference("performances")

  private fun child(index: Int): DatabaseReference = performances.child(index.toString())

  private fun rating(index: Int, userID: String): DatabaseReference =
    child(index).child("ratings/$userID")

  private fun comment(index: Int, userID: String): DatabaseReference =
    child(index).child("comments/$userID")

  private operator fun DataSnapshot.get(key: String): String? = child(key).getValue<String>()

  val countFlow: Flow<Int> = performances.snapshots.map { it.childrenCount.toInt() }

  operator fun get(index: Int): Flow<Performance> = child(index).snapshots.map {
    Performance(
      id = index,
      name = it["name"],
      city = it["city"],
      category = it["category"],
      performance = it["performance"],
      age = it.child("age").getValue<Long>(),
      nomination = it["nomination"],
    )
  }

  fun getRating(index: Int, userID: String): Flow<Double?> =
    rating(index, userID).snapshots.map { it.getValue<Double>() }

  suspend fun rate(index: Int, userID: String, rating: Double) {
    rating(index, userID).setValue(rating).await()
  }

  fun getComment(index: Int, userID: String): Flow<String?> =
    comment(index, userID).snapshots.map { it.getValue<String>() }

  suspend fun comment(index: Int, userID: String, comment: String) {
    comment(index, userID).setValue(comment).await()
  }
}