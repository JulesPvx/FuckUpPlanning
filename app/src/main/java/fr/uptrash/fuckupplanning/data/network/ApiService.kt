package fr.uptrash.fuckupplanning.data.network

import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * API service interface for UPLanning application
 * Base URL: https://upplanning.appli.univ-poitiers.fr/
 *
 * This interface defines the contract for API communication.
 * Actual endpoints will be implemented based on specific requirements.
 */
interface ApiService {

    /**
     * Fetches S1 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21212&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS1ICalData(): String

    /**
     * Fetches S2 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21298&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS2ICalData(): String
}