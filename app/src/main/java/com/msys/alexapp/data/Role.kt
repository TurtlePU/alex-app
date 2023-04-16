package com.msys.alexapp.data

enum class Role {
  ADMIN, STAGE, JURY;

  override fun toString() = when (this) {
    ADMIN -> "admin"
    STAGE -> "stage"
    JURY -> "jury"
  }
}