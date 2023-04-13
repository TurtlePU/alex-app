package com.msys.alexapp.services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.JuryService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseJuryService(private val stageID: String) : JuryService {
  companion object {
    private val jury: DatabaseReference get() = data.child(currentUID)
  }

  private val stage: DatabaseReference get() = data.child(stageID)
  override val currentPerformance: Flow<Performance?>
    get() = stage.child("current").snapshots.map { it.children.firstOrNull()?.asPerformance }
  override val performanceCount: Flow<Long>
    get() = stage.child("done").snapshots.map { it.childrenCount }
  override val canComment: Flow<Boolean>
    get() = stage.child("comment").snapshots.map { it.exists() && it.value == true }
  override val deadline: Flow<Date>
    get() = stage.child("deadline").snapshots.map { Date(it.getValue<Long>()!!) }

  override fun isEvaluated(id: String): Flow<Boolean> = jury.child(id).snapshots.map { it.exists() }

  override fun averageRating(id: String) = averageRatingFlow(id)

  override suspend fun sendInvitation() {
    val contacts = stage.child("contacts").get().await()
    jury.chooseFriends(Role.JURY, contacts.children.map { it.getValue<String>()!! })
  }

  override suspend fun evaluate(id: String, rating: Double, comment: String?) {
    jury.child(id).setValue(mapOf("rating" to rating, "comment" to comment)).await()
  }
}