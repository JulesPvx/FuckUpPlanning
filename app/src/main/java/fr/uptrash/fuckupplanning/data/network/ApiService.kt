package fr.uptrash.fuckupplanning.data.network

import retrofit2.http.GET

/**
 * API service interface for UPLanning application
 * Base URL: https://upplanning.appli.univ-poitiers.fr/
 *
 * This interface defines the contract for API communication.
 * Actual endpoints will be implemented based on specific requirements.
 */
interface ApiService {

    /**
     * Fetches iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=24835&projectId=16&calType=ical&nbWeeks=15")
    suspend fun getICalData(): String
}