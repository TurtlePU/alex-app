package com.msys.alexapp.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.StagePreparationService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.tasks.await

class FirebaseStagePreparationService(private val adminID: String) : StagePreparationService {
  companion object {
    private val stage: DatabaseReference get() = data.child(currentUID)
    private val staged: DatabaseReference get() = stage.child("stage")

    private val DatabaseReference.performances: Flow<List<Performance>>
      get() = child("performances").snapshots.map {
        it.children.map(DataSnapshot::asPerformance)
      }
  }

  private val admin: DatabaseReference get() = data.child(adminID)
  override val performancesFlow: Flow<List<Performance>>
    get() = admin.performances.zip(stage.performances) { a, b -> a + b }
  override val stagedFlow: Flow<List<String>>
    get() = staged.snapshots.map { staged ->
      staged.children
        .sortedBy { it.key }
        .map { it.getValue<String>()!! }
    }

  override suspend fun sendInvitations() {
    val contacts = admin.child("contacts").get().await()
    val task = stage.child("contacts").setValue(contacts.getValue<Map<String, String>>()!!)
    stage.chooseFriends(Role.STAGE, contacts.children.map { it.getValue<String>()!! })
    task.await()
  }

  override suspend fun newPerformance(performance: Performance) {
    stage.child("performances/${performance.id}").setValue(performance.toMap()).await()
  }

  override suspend fun appendToStage(stage: List<String>) {
    val lastKey = staged.get().await().children.maxOfOrNull { it.key!!.toInt() } ?: -1
    staged.updateChildren(stage.mapIndexed { i, s -> (lastKey + i + 1).toString() to s }.toMap())
  }
}