package com.msys.alexapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val email: String get() = FirebaseAuth.getInstance().currentUser!!.email!!

fun friendIdsByRole(role: String): Flow<List<String>> =
  FirebaseDatabase.getInstance().getReference("invitations/$email").snapshots.map { data ->
    data.children.filter { it.value == role }.map { it.key!! }
  }