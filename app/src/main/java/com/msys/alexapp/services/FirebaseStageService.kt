package com.msys.alexapp.services

import com.msys.alexapp.components.StageService
import com.msys.alexapp.data.Advice
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.flow.Flow

class FirebaseStageService(private val adminID: String) : StageService {
  override val stagedFlow: Flow<Map<Long, String>>
    get() = TODO("Not yet implemented")

  override fun performance(id: String): Flow<Performance> {
    TODO("Not yet implemented")
  }

  override suspend fun sendAdvice(advice: Advice) {
    TODO("Not yet implemented")
  }

  override suspend fun setCurrent(performance: Performance) {
    TODO("Not yet implemented")
  }
}