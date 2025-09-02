package fr.uptrash.fuckupplanning.util

import fr.uptrash.fuckupplanning.data.model.Event
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

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
            }
            i++
        }

        return if (uid.isNotEmpty() && startDateTime != null && endDateTime != null) {
            Event(uid, summary, description, startDateTime, endDateTime, location)
        } else null
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime? {
        return try {
            // iCal format: YYYYMMDDTHHMMSS or YYYYMMDDTHHMMSSZ
            val cleanedString = dateTimeString.replace("Z", "").trim()

            if (cleanedString.length >= 15) {
                val year = cleanedString.substring(0, 4).toInt()
                val month = cleanedString.substring(4, 6).toInt()
                val day = cleanedString.substring(6, 8).toInt()
                val hour = cleanedString.substring(9, 11).toInt()
                val minute = cleanedString.substring(11, 13).toInt()
                val second =
                    if (cleanedString.length >= 15) cleanedString.substring(13, 15).toInt() else 0

                LocalDateTime(year, month, day, hour, minute, second)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}