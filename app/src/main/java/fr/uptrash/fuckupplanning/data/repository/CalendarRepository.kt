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
            val iCalData = apiService.getICalData()
            val events = parser.parseICalData(iCalData)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}