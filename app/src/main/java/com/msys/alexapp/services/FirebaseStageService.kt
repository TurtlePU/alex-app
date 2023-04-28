package com.msys.alexapp.services

import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.JuryNote
import com.msys.alexapp.components.StageService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.JuryReport.Companion.asJuryReport
import com.msys.alexapp.data.Role
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseStageService(adminID: String) : FirebaseStageServiceBase(adminID), StageService {
  override val canCommentFlow: Flow<Boolean>
    get() = admin.child("canComment").snapshots.map {
      it.exists() && it.getValue<Boolean>() == true
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override val firstStagedPerformance: Flow<Pair<String, Performance>?>
    get() = staged.orderByKey().limitToFirst(1).snapshots.flatMapLatest { stagedList ->
      stagedList.children.firstOrNull()?.let { firstID ->
        val key = firstID.key!!
        val id = firstID.getValue<String>()!!
        admin.child("performances/$id").snapshots.flatMapLatest { fromAdmin ->
          if (fromAdmin.exists()) flowOf(key to fromAdmin.asPerformance)
          else stage.child("performances/$id").snapshots.map { key to it.asPerformance }
        }
      } ?: flowOf(null)
    }

  override val nextStagedPerformance: Flow<String?>
    get() = staged.orderByKey().limitToFirst(2).snapshots.map {
      it.children.drop(1).firstOrNull()?.let { child -> child.getValue<String>()!! }
    }
  override val juryIDs: Flow<List<String>> get() = FirebaseService.invitationsFrom(Role.JURY)

  override fun readNote(juryID: String, performanceID: String): Flow<JuryNote?> =
    data.child(juryID).snapshots.map { jury ->
      jury.child("nickname").getValue<String>()
        ?.let { JuryNote(it, jury.child("report/$performanceID").asJuryReport) }
    }

  override suspend fun dropStaged(key: String) {
    staged.child(key).removeValue().await()
  }

  override suspend fun setCanComment(canComment: Boolean) {
    stage.child("advice/canComment").setValue(canComment).await()
  }

  override suspend fun setCurrent(performance: Performance, deadline: Date) {
    val task = stage.child("current").setValue(mapOf(performance.id to performance.toMap()))
    stage.child("advice/deadline").setValue(deadline.time).await()
    task.await()
  }

  override suspend fun fetchDeadline() =
    Date(stage.child("advice/deadline").get().await().getValue<Long>() ?: 0)

  override suspend fun sendAverageRating(performanceID: String, averageRating: Double) {
    if (!averageRating.isNaN()) {
      stage.child("report/$performanceID/average").setValue(averageRating).await()
    }
  }

  override suspend fun publishComment(
    performanceID: String,
    juryNickname: String,
    comment: String
  ) {
    stage.child("report/$performanceID/comments/$juryNickname").setValue(comment).await()
  }
}