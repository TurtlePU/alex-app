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
  override val stagedFlow: Flow<Map<Long, String>>
    get() = staged.snapshots.map {
      it.children.associate { child -> child.key!!.toLong() to child.getValue<String>()!! }
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun performance(id: String): Flow<Performance> =
    stage.child("performances/$id").snapshots.flatMapLatest { fromStage ->
      if (fromStage.exists()) flowOf(fromStage.asPerformance)
      else admin.child("performances/$id").snapshots.map { it.asPerformance }
    }

  override suspend fun sendAdvice(advice: Advice) {
    stage.child("advice").setValue(advice.toMap()).await()
  }

  override suspend fun setCurrent(performance: Performance) {
    stage.child("current").setValue(mapOf(performance.id to performance.toMap())).await()
  }
}