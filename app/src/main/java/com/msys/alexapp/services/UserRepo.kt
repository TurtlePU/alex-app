package com.msys.alexapp.services

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class User(val id: String, val name: String)

val usersFlow: Flow<List<User>> =
  FirebaseDatabase.getInstance().getReference("jury").snapshots.map {
    it.children.map { userEntry -> User(userEntry.key!!, userEntry.getValue<String>()!!) }
  }