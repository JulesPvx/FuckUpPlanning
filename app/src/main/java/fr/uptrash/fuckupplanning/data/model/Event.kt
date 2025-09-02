package fr.uptrash.fuckupplanning.data.model

import kotlinx.datetime.LocalDateTime

data class Event(
    val uid: String,
    val summary: String,
    val description: String?,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val location: String?
)