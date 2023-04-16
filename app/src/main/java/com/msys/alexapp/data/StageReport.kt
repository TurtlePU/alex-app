package com.msys.alexapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue

data class StageReport(val averageRating: Double, val comments: Map<String, String>) {
  companion object {
    val DataSnapshot.asStageReport: StageReport get() = asStageReportOrNull!!
    val DataSnapshot.asStageReportOrNull: StageReport?
      get() {
        val rating = child("average").getValue<Double>() ?: return null
        val comments = child("comments").getValue<Map<String, String>>() ?: mapOf()
        return StageReport(rating, comments)
      }
  }
}