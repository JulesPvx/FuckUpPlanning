package fr.uptrash.fuckupplanning.data.repository

import fr.uptrash.fuckupplanning.data.model.Event
import fr.uptrash.fuckupplanning.data.network.ApiService
import fr.uptrash.fuckupplanning.util.ICalParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val apiService: ApiService,
    private val parser: ICalParser
) {
    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            // Fetch both S1 and S2 calendar data
            val s1ICalData = apiService.getS1ICalData()
            val s2ICalData = apiService.getS2ICalData()

            // Parse events from both calendars
            val s1Events = parser.parseICalData(s1ICalData)
            val s2Events = parser.parseICalData(s2ICalData)

            // Combine and sort all events by start time
            val allEvents = (s1Events + s2Events).sortedBy { it.startDateTime }

            Result.success(allEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}