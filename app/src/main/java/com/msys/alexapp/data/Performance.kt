package com.msys.alexapp.data

data class Performance(
  val id: String,
  val name: String,
  val performance: String,
  val city: String?,
  val category: String?,
  val age: Long?,
  val nomination: String?,
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "performance" to performance,
    "city" to city,
    "category" to category,
    "age" to age,
    "nomination" to nomination,
  )
}