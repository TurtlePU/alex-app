package com.msys.alexapp.data

import java.util.*

data class Advice(
  val deadline: Date,
  val canComment: Boolean = false,
) {
  fun toMap(): Map<String, Any> = mapOf(
    "deadline" to deadline.time,
    "canComment" to canComment,
  )
}