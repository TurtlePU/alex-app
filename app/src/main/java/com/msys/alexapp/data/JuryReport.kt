package com.msys.alexapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue

data class JuryReport(
  val rating: Double,
  val comment: String?,
) {
  companion object {
    val DataSnapshot.asJuryReport
      get() : JuryReport? {
        return JuryReport(
          child("rating").getValue<Double>() ?: return null,
          child("comment").getValue<String>()
        )
      }
  }

  fun toMap(): Map<String, Any?> = mapOf(
    "rating" to rating,
    "comment" to comment
  )
}