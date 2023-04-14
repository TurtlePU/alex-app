package com.msys.alexapp.services

import com.google.firebase.database.DatabaseReference

open class FirebaseStageServiceBase(private val adminID: String) {
  companion object {
    @JvmStatic
    protected val stage: DatabaseReference get() = data.child(currentUID)
    @JvmStatic
    protected val staged: DatabaseReference get() = stage.child("stage")
  }

  protected val admin: DatabaseReference get() = data.child(adminID)
}