package com.msys.alexapp.services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Performance(val name: String)

object PerformanceRepo {
  private val performances: DatabaseReference =
    FirebaseDatabase.getInstance().getReference("performances")

  val countFlow: Flow<Int> = performances.snapshots.map { it.childrenCount.toInt() }

  operator fun get(index: Int): Flow<Performance> =
    performances.child(index.toString()).snapshots.map {
      Performance(
        it.child("name").getValue<String>()!!
      )
    }

  suspend fun rate(index: Int, userID: String, rating: Double) {
    TODO()
  }

  suspend fun comment(index: Int, userID: String, comment: String) {
    TODO()
  }
}