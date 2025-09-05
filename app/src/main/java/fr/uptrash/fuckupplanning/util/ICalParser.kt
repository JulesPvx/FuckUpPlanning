package fr.uptrash.fuckupplanning.util

import fr.uptrash.fuckupplanning.data.model.Event
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class ICalParser @Inject constructor() {

    fun parseICalData(iCalData: String): List<Event> {
        val events = mutableListOf<Event>()
        val lines = iCalData.lines()

        var i = 0
        while (i < lines.size) {
            if (lines[i] == "BEGIN:VEVENT") {
                val event = parseEvent(lines, i)
                if (event != null) {
                    events.add(event)
                }
            }
            i++
        }

        return events.sortedBy { it.startDateTime }
    }

    private fun parseEvent(lines: List<String>, startIndex: Int): Event? {
        var uid = ""
        var summary = ""
        var description: String? = null
        var startDateTime: LocalDateTime? = null
        var endDateTime: LocalDateTime? = null
        var location: String? = null
        var dtstamp: String? = null
        var created: String? = null
        var lastModified: String? = null
        var sequence: String? = null

        var i = startIndex + 1
        while (i < lines.size && lines[i] != "END:VEVENT") {
            val line = lines[i]
            when {
                line.startsWith("UID:") -> uid = line.substringAfter("UID:")
                line.startsWith("SUMMARY:") -> summary = line.substringAfter("SUMMARY:")
                line.startsWith("DESCRIPTION:") -> description = line.substringAfter("DESCRIPTION:")
                line.startsWith("DTSTART:") -> {
                    startDateTime = parseDateTime(line.substringAfter("DTSTART:"))
                }

                line.startsWith("DTEND:") -> {
                    endDateTime = parseDateTime(line.substringAfter("DTEND:"))
                }

                line.startsWith("LOCATION:") -> location = line.substringAfter("LOCATION:")
                line.startsWith("DTSTAMP:") -> dtstamp = line.substringAfter("DTSTAMP:")
                line.startsWith("CREATED:") -> created = line.substringAfter("CREATED:")
                line.startsWith("LAST-MODIFIED:") -> lastModified =
                    line.substringAfter("LAST-MODIFIED:")

                line.startsWith("SEQUENCE:") -> sequence = line.substringAfter("SEQUENCE:")
            }
            i++
        }

        val parsedDescription = parseDescription(description)

        return if (uid.isNotEmpty() && startDateTime != null && endDateTime != null) {
            Event(
                uid = uid,
                summary = formatEventTitle(summary),
                description = parsedDescription.originalDescription,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                location = formatLocation(location?.replace("\\,", ",")),
                courseType = parsedDescription.courseType,
                instructor = parsedDescription.instructor,
                groups = parsedDescription.groups,
                notes = parsedDescription.notes,
                lastUpdated = parsedDescription.lastUpdated,
                dtstamp = dtstamp,
                created = created,
                lastModified = lastModified,
                sequence = sequence
            )
        } else null
    }

    private data class ParsedDescription(
        val originalDescription: String?,
        val courseType: String?,
        val instructor: String?,
        val groups: List<String>,
        val notes: String?,
        val lastUpdated: String?
    )

    private fun parseDescription(description: String?): ParsedDescription {
        if (description.isNullOrBlank()) {
            return ParsedDescription(description, null, null, emptyList(), null, null)
        }

        // Clean up the description by removing escape sequences
        val cleanDescription = description.replace("\\n", "\n").replace("\\,", ",")
        val lines = cleanDescription.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        var courseType: String? = null
        var instructor: String? = null
        var lastUpdated: String? = null
        val groups = mutableListOf<String>()
        val notesList = mutableListOf<String>()

        for (line in lines) {
            when {
                // Check for course types (CM, TD, TDB, TDA, TP with numbers) - merge all into courseType
                line.matches(Regex("^(CM|TDB?|TDA|TD\\d*|TP\\d+)$")) -> {
                    courseType = line
                }
                // Also check for course types that might be embedded in other text patterns
                line.contains(Regex("\\b(CM|TDB?|TDA|TD\\d*|TP\\d+)\\b")) -> {
                    val courseTypeMatches = Regex("\\b(CM|TDB?|TDA|TD\\d*|TP\\d+)\\b").findAll(line)
                    courseTypeMatches.forEach { match ->
                        if (courseType == null) {
                            courseType = match.value
                        }
                    }
                    // If the line contains other text, add it to notes
                    val lineWithoutCourseType =
                        line.replace(Regex("\\b(CM|TDB?|TDA|TD\\d*|TP\\d+)\\b"), "").trim()
                    if (lineWithoutCourseType.isNotEmpty() && !lineWithoutCourseType.matches(Regex("^\\s*$"))) {
                        notesList.add(lineWithoutCourseType)
                    }
                }
                // Check for instructor names (all caps format with space)
                line.matches(Regex("^[A-Z]+ [A-Z]+$")) && !line.contains("BUT") -> {
                    instructor = line
                }
                // Check for update timestamp
                line.startsWith("(Updated :") && line.endsWith(")") -> {
                    lastUpdated = line.removePrefix("(Updated :").removeSuffix(")")
                }
                // Everything else goes to notes
                else -> {
                    if (line.isNotEmpty() && !line.matches(Regex("^\\s*$"))) {
                        notesList.add(line)
                    }
                }
            }
        }

        val notes = if (notesList.isNotEmpty()) notesList.joinToString("\n") else null

        return ParsedDescription(
            originalDescription = description,
            courseType = courseType,
            instructor = instructor,
            groups = groups,
            notes = notes,
            lastUpdated = lastUpdated
        )
    }

    private fun formatEventTitle(title: String): String {
        // Only format titles that start with R followed by digits, dot, digits (like R3.12-dev front-info)
        if (title.matches(Regex("^R\\d+\\.\\d+.*"))) {
            // Split on hyphen to separate room/code from subject
            val parts = title.split("-", limit = 2)
            if (parts.size == 2) {
                val roomCode = parts[0].trim() // e.g., "R3.12"
                val subject = parts[1].trim() // e.g., "dev front-info"

                // Capitalize first letter of each word in the subject
                val formattedSubject = subject.split(" ").joinToString(" ") { word ->
                    word.lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }

                return "$roomCode - $formattedSubject"
            }
        }

        // Return original title if it doesn't match the pattern or doesn't contain a hyphen
        return title
    }

    private fun formatLocation(location: String?): String? {
        if (location.isNullOrBlank()) return location

        // Handle multiple rooms separated by commas without spaces (e.g., "MMI300,MMI301")
        return location.split(",").joinToString(", ") { room ->
            room.trim()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun parseDateTime(dateTimeString: String): LocalDateTime? {
        return try {
            // iCal format: YYYYMMDDTHHMMSS or YYYYMMDDTHHMMSSZ
            val isUtc = dateTimeString.endsWith("Z")
            val cleanedString = dateTimeString.replace("Z", "").trim()

            if (cleanedString.length >= 13) {
                val year = cleanedString.substring(0, 4).toInt()
                val month = cleanedString.substring(4, 6).toInt()
                val day = cleanedString.substring(6, 8).toInt()
                val hour = cleanedString.substring(9, 11).toInt()
                val minute = cleanedString.substring(11, 13).toInt()
                val second = if (cleanedString.length >= 15) {
                    cleanedString.substring(13, 15).toInt()
                } else {
                    0
                }

                val localDateTime = LocalDateTime(year, month, day, hour, minute, second)

                // If the timestamp is in UTC (ends with Z), convert to local time
                if (isUtc) {
                    val instant = localDateTime.toInstant(kotlinx.datetime.TimeZone.UTC)
                    instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                } else {
                    localDateTime
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}