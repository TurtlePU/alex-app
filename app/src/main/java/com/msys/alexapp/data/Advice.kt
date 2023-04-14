package com.msys.alexapp.data

import java.util.*

data class Advice(
  val deadline: Date,
  val index: Long = 0,
  val canComment: Boolean = false,
) {
  fun toMap(): Map<String, Any> = mapOf(
    "index" to index,
    "deadline" to deadline.time,
    "canComment" to canComment,
  )
}