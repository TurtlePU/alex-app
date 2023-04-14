package com.msys.alexapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue

data class Report(
  val rating: Double,
  val comment: String?,
) {
  companion object {
    val DataSnapshot.asReport
      get() = Report(
        child("rating").getValue<Double>()!!,
        child("comment").getValue<String>()
      )
  }

  fun toMap(): Map<String, Any?> = mapOf(
    "rating" to rating,
    "comment" to comment
  )
}