package fr.uptrash.fuckupplanning.data.model

import fr.uptrash.fuckupplanning.data.repository.MMIYear
import fr.uptrash.fuckupplanning.data.repository.TPGroup

data class Homework(
    val id: String = "",
    val description: String = "",
    val dueDate: Long = 0L, // Timestamp
    val isCompleted: Boolean = false,
    val tp: TPGroup = TPGroup.ALL,
    val year: MMIYear = MMIYear.MMI1,
    val createdAt: Long = System.currentTimeMillis(),
    val ownerId: String = "Unknown"
)