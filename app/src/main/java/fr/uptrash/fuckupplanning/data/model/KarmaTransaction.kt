package fr.uptrash.fuckupplanning.data.model

data class KarmaTransaction(
    val id: String = "",
    val userId: String = "", // User whose karma is being affected
    val homeworkId: String = "", // Homework that caused the karma change
    val karmaChange: Int = 0, // +1 for upvote, -1 for downvote
    val timestamp: Long = System.currentTimeMillis(),
    val type: KarmaTransactionType = KarmaTransactionType.VOTE,
    val voterUserId: String = "" // User who voted (for votes)
)

enum class KarmaTransactionType {
    VOTE, // Karma from receiving votes
    BONUS, // Any bonus karma
    PENALTY // Any penalty karma
}
