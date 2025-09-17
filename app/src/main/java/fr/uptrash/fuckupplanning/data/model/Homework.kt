package fr.uptrash.fuckupplanning.data.model

import fr.uptrash.fuckupplanning.data.repository.MMIYear
import fr.uptrash.fuckupplanning.data.repository.TPGroup

data class Homework(
    val id: String = "",
    val description: String = "",
    val dueDate: Long = 0L, // Timestamp
    val tp: TPGroup = TPGroup.ALL,
    val year: MMIYear = MMIYear.MMI1,
    val createdAt: Long = System.currentTimeMillis(),
    val ownerId: String = "Unknown",
    val karma: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val upvotes: Map<String, Boolean> = emptyMap(), // userId -> true for upvote
    val downvotes: Map<String, Boolean> = emptyMap() // userId -> true for downvote
) {
    fun netVotes(): Int = (upvotes.size - downvotes.size)

    fun isUpvotedBy(userId: String?): Boolean = upvotes.containsKey(userId)
    fun isDownvotedBy(userId: String?): Boolean = downvotes.containsKey(userId)

    fun canEdit(userId: String?): Boolean = userId == ownerId
}