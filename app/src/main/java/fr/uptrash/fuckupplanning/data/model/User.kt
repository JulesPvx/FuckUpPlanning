package fr.uptrash.fuckupplanning.data.model

data class User(
    val id: String = "",
    val totalKarma: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val displayName: String = "Anonymous User"
)
