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

    /* MMI 1 */

    /**
     * Fetches S1 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=18300&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS1MMI1ICalData(): String

    /**
     * Fetches S2 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21211&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS2MMI1ICalData(): String

    /* MMI 2 */

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
    suspend fun getS1MMI2ICalData(): String

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
    suspend fun getS2MMI2ICalData(): String

    /* MMI 3 */

    /**
     * Fetches S1 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=2450&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS1MMI3ICalData(): String

    /**
     * Fetches S2 iCal data from the UPLanning application.
     * Returns the raw iCal data as a String.
     */
    @Headers(
        "Cache-Control: no-cache, no-store, must-revalidate",
        "Pragma: no-cache",
        "Expires: 0"
    )
    @GET("jsp/custom/modules/plannings/anonymous_cal.jsp?resources=2471&projectId=16&calType=ical&nbWeeks=150")
    suspend fun getS2MMI3ICalData(): String
}