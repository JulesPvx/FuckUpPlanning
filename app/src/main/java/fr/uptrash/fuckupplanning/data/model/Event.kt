package fr.uptrash.fuckupplanning.data.model

import kotlinx.datetime.LocalDateTime

data class Event(
    val uid: String,
    val summary: String,
    val description: String?,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val location: String?,
    val courseType: String? = null,
    val instructor: String? = null,
    val groups: List<String> = emptyList(), // For TP1, TP2, TP3, TP4 etc.
    val notes: String? = null,
    val lastUpdated: String? = null,
    val dtstamp: String? = null,
    val created: String? = null,
    val lastModified: String? = null,
    val sequence: String? = null
)