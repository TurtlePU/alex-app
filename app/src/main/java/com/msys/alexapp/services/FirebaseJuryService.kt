package com.msys.alexapp.services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.Advice
import com.msys.alexapp.components.JuryService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.JuryReport
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
  override val juryAdvice: Flow<Advice>
    get() = stage.child("advice").snapshots.map {
      Advice(
        deadline = Date(it.child("deadline").getValue<Long>()!!),
        canComment = it.child("canComment").run { exists() && value == true },
      )
    }

  override fun isEvaluated(id: String): Flow<Boolean> = jury.child(id).snapshots.map { it.exists() }

  override fun averageRating(id: String) =
    stage.child("report/$id/average").snapshots.map { it.getValue<Double>() }

  override suspend fun sendInvitation() {
    val contacts = stage.child("contacts").get().await().getValue<Map<String, String>>()!!
    jury.child("nickname").setValue(contacts[currentEmail]).await()
    jury.chooseFriends(Role.JURY, contacts)
  }

  override suspend fun evaluate(id: String, report: JuryReport) {
    jury.child("report/$id").setValue(report.toMap()).await()
  }
}