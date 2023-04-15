package com.msys.alexapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue

data class StageReport(val averageRating: Double, val comments: Map<String, String>) {
  companion object {
    val DataSnapshot.asStageReport
      get() = StageReport(
        averageRating = child("rating").getValue<Double>()!!,
        comments = child("comments").getValue<Map<String, String>>() ?: mapOf(),
      )
  }
}