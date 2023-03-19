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
  val name: String,
  val city: String,
  val category: String,
  val performance: String,
  val age: Long,
  val nomination: String,
)

object PerformanceRepo {
  private val performances: DatabaseReference =
    FirebaseDatabase.getInstance().getReference("performances")

  private operator fun DataSnapshot.get(key: String): String = child(key).getValue<String>()!!

  val countFlow: Flow<Int> = performances.snapshots.map { it.childrenCount.toInt() }

  operator fun get(index: Int): Flow<Performance> =
    performances.child(index.toString()).snapshots.map {
      Performance(
        id = index,
        name = it["name"],
        city = it["city"],
        category = it["category"],
        performance = it["performance"],
        age = it.child("age").getValue<Long>()!!,
        nomination = it["nomination"],
      )
    }

  suspend fun rate(index: Int, userID: String, rating: Double) {
    performances.child("$index/ratings/$userID").setValue(rating).await()
  }

  suspend fun comment(index: Int, userID: String, comment: String) {
    performances.child("$index/comments/$userID").setValue(comment).await()
  }
}