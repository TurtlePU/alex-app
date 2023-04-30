package com.msys.alexapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue
import com.msys.alexapp.data.JuryReport.Companion.asJuryReport

data class StageReport(val averageRating: Double, val comments: Map<String, JuryReport>) {
  companion object {
    val DataSnapshot.asStageReport: StageReport get() = asStageReportOrNull!!
    val DataSnapshot.asStageReportOrNull: StageReport?
      get() {
        val rating = child("average").getValue<Double>() ?: return null
        val comments = child("comments").children.associate { it.key!! to it.asJuryReport!! }
        return StageReport(rating, comments)
      }
  }
}