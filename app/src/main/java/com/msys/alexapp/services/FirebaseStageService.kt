package com.msys.alexapp.services

import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.StageService
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.Report
import com.msys.alexapp.data.Report.Companion.asReport
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
  override val firstStagedPerformance: Flow<Performance?>
    get() = staged.orderByKey().limitToFirst(1).snapshots.flatMapLatest { stagedList ->
      stagedList.children.firstOrNull()?.let { firstID ->
        val id = firstID.getValue<String>()!!
        admin.child("performances/$id").snapshots.flatMapLatest { fromAdmin ->
          if (fromAdmin.exists()) flowOf(fromAdmin.asPerformance)
          else stage.child("performances/$id").snapshots.map { it.asPerformance }
        }
      } ?: flowOf(null)
    }

  override val nextStagedPerformance: Flow<String?>
    get() = staged.orderByKey().limitToFirst(2).snapshots.map {
      it.children.drop(1).firstOrNull()?.let { child -> child.getValue<String>()!! }
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun reportsFlow(performanceID: String): Flow<Map<String, Report?>> =
    FirebaseService
      .invitationsFrom(Role.JURY)
      .flatMapLatest {
        combine(it.map { jury ->
          data
            .child("$jury/report/$performanceID")
            .snapshots
            .map { report -> jury to report.asReport }
        }, Array<Pair<String, Report>>::toMap)
      }

  override suspend fun setCanComment(canComment: Boolean) {
    stage.child("advice/canComment").setValue(canComment).await()
  }

  override suspend fun setCurrent(performance: Performance, deadline: Date) {
    val task = stage.child("current").setValue(mapOf(performance.id to performance.toMap()))
    stage.child("advice/deadline").setValue(deadline.time).await()
    task.await()
  }

  override suspend fun sendAverageRating(performanceID: String, averageRating: Double?) {
    stage.child("report/$performanceID/average").setValue(averageRating).await()
  }

  override suspend fun publishComments(performanceID: String, comments: Map<String, String>) {
    stage.child("report/$performanceID/comments").setValue(comments).await()
  }
}