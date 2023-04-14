package com.msys.alexapp.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.StageService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.tasks.await

class FirebaseStageService(private val adminID: String) : StageService {
  companion object {
    private val stage: DatabaseReference get() = data.child(currentUID)

    private val DatabaseReference.performances: Flow<List<Performance>>
      get() = child("performances").snapshots.map {
        it.children.map(DataSnapshot::asPerformance)
      }
  }

  private val admin: DatabaseReference get() = data.child(adminID)
  override val performancesFlow: Flow<List<Performance>>
    get() = admin.performances.zip(stage.performances) { a, b -> a + b }

  override suspend fun sendInvitations() {
    val contacts = admin.child("contacts").get().await()
    stage.chooseFriends(Role.STAGE, contacts.children.map { it.getValue<String>()!! })
  }

  override suspend fun newPerformance(performance: Performance) {
    performance.run {
      stage
        .child("performances/$id")
        .setValue(
          mapOf(
            "name" to name,
            "performance" to this.performance,
            "city" to city,
            "category" to category,
            "age" to age,
            "nomination" to nomination,
          )
        )
        .await()
    }
  }

  override suspend fun setStage(stage: List<String>) {
    Companion.stage
      .child("stage")
      .setValue(
        stage
          .mapIndexed { i, s -> i.toString() to s }
          .toMap()
      )
      .await()
  }
}