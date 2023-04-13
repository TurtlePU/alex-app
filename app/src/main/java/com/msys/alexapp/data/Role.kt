package com.msys.alexapp.data

enum class Role {
  ADMIN, STAGE, JURY;

  override fun toString() = when (this) {
    ADMIN -> "admin"
    STAGE -> "stage"
    JURY -> "jury"
  }

  companion object {
    fun String.toRole(): Role? = when (this) {
      "admin" -> ADMIN
      "stage" -> STAGE
      "jury" -> JURY
      else -> null
    }
  }
}