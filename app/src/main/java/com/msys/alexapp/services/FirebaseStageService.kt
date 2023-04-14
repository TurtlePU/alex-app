package com.msys.alexapp.services

import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.msys.alexapp.components.StageService
import com.msys.alexapp.data.Advice
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseStageService(adminID: String) : FirebaseStageServiceBase(adminID), StageService {
  override val canCommentFlow: Flow<Boolean>
    get() = admin.child("canComment").snapshots.map {
      it.exists() && it.getValue<Boolean>() == true
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override val firstStagedPerformance: Flow<Pair<Long, Performance>?>
    get() = staged.orderByKey().limitToFirst(1).snapshots.flatMapLatest { stagedList ->
      stagedList.children.firstOrNull()?.let { firstID ->
        val key = firstID.key!!.toLong()
        val id = firstID.getValue<String>()!!
        stage.child("performances/$id").snapshots.flatMapLatest { fromStage ->
          if (fromStage.exists()) flowOf(key to fromStage.asPerformance)
          else admin.child("performances/$id").snapshots.map { key to it.asPerformance }
        }
      } ?: flowOf(null)
    }

  override val nextStagedPerformance: Flow<String?>
    get() = staged.orderByKey().limitToFirst(2).snapshots.map {
      it.children.drop(1).firstOrNull()?.let { child -> child.getValue<String>()!! }
    }

  override suspend fun sendAdvice(advice: Advice) {
    stage.child("advice").setValue(advice.toMap()).await()
  }

  override suspend fun setCurrent(performance: Performance) {
    stage.child("current").setValue(mapOf(performance.id to performance.toMap())).await()
  }
}