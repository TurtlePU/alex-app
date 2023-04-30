package com.msys.alexapp.data

import java.util.SortedMap

data class Summary(
  val id: String,
  val name: String,
  val performance: String,
  val rating: Double,
  val degree: String,
  val comments: Map<String, JuryReport>
) {
  companion object {
    fun StageReport.toSummary(
      performance: Performance,
      degree: (Double) -> String
    ): Summary =
      performance.run {
        Summary(id, name, performance.performance, averageRating, degree(averageRating), comments)
      }

    fun SortedMap<Double, String>.matching(rating: Double): String =
      tailMap(rating).values.firstOrNull() ?: "??"
  }
}