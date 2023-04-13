package com.msys.alexapp.repo

import com.google.firebase.auth.FirebaseAuth
import com.msys.alexapp.components.AlexAppService
import kotlinx.coroutines.tasks.await

object FirebaseService : AlexAppService {
  override suspend fun signIn(email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
  }

  override fun invitationsFrom(role: Role) = friendIdsByRole(role)
}