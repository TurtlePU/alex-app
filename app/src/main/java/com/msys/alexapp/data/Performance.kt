package com.msys.alexapp.data

data class Performance(
  val id: String,
  val name: String,
  val performance: String,
  val city: String?,
  val category: String?,
  val age: Long?,
  val nomination: String?,
)