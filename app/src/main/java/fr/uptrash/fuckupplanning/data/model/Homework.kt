package fr.uptrash.fuckupplanning.data.model

data class Homework(
    val id: String = "",
    val description: String = "",
    val dueDate: Long = 0L, // Timestamp
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)