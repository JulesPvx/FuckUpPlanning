package fr.uptrash.fuckupplanning.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cookie manager for handling session cookies in UPLanning app.
 * Since the API uses sessions and GWT, proper cookie management is crucial.
 * 
 * This implementation uses SharedPreferences to persist cookies across app restarts.
 */
@Singleton
class CookieManager @Inject constructor(
    private val context: Context
) : CookieJar {
    
    companion object {
        private const val TAG = "CookieManager"
        private const val PREFS_NAME = "uplanning_cookies"
        private const val COOKIE_SEPARATOR = ";;;"
    }
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Log.d(TAG, "Saving ${cookies.size} cookies for ${url.host}")
        
        val urlKey = url.host
        val cookieStrings = mutableSetOf<String>()
        
        // Load existing cookies for this host
        val existingCookies = preferences.getStringSet(urlKey, mutableSetOf()) ?: mutableSetOf()
        
        // Convert existing cookies to map for easier management
        val existingCookieMap = mutableMapOf<String, String>()
        existingCookies.forEach { cookieString ->
            val cookie = parseCookieString(cookieString)
            if (cookie != null) {
                existingCookieMap[cookie.name] = cookieString
            }
        }
        
        // Add or update cookies
        cookies.forEach { cookie ->
            val cookieString = cookieToString(cookie)
            existingCookieMap[cookie.name] = cookieString
            Log.d(TAG, "Saved cookie: ${cookie.name} = ${cookie.value}")
        }
        
        cookieStrings.addAll(existingCookieMap.values)
        
        preferences.edit()
            .putStringSet(urlKey, cookieStrings)
            .apply()
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        Log.d(TAG, "Loading cookies for ${url.host}")
        
        val urlKey = url.host
        val cookieStrings = preferences.getStringSet(urlKey, emptySet()) ?: emptySet()
        
        val cookies = mutableListOf<Cookie>()
        
        cookieStrings.forEach { cookieString ->
            val cookie = parseCookieString(cookieString)
            if (cookie != null) {
                // Check if cookie is still valid
                if (System.currentTimeMillis() < cookie.expiresAt) {
                    cookies.add(cookie)
                    Log.d(TAG, "Loaded cookie: ${cookie.name} = ${cookie.value}")
                } else {
                    Log.d(TAG, "Cookie expired: ${cookie.name}")
                }
            }
        }
        
        Log.d(TAG, "Loaded ${cookies.size} valid cookies for ${url.host}")
        return cookies
    }
    
    /**
     * Convert Cookie to String for storage
     */
    private fun cookieToString(cookie: Cookie): String {
        return "${cookie.name}=${cookie.value};domain=${cookie.domain};path=${cookie.path};" +
                "expires=${cookie.expiresAt};secure=${cookie.secure};httponly=${cookie.httpOnly}"
    }
    
    /**
     * Parse Cookie from String
     */
    private fun parseCookieString(cookieString: String): Cookie? {
        return try {
            val parts = cookieString.split(";")
            if (parts.isEmpty()) return null
            
            val nameValue = parts[0].split("=", limit = 2)
            if (nameValue.size != 2) return null
            
            val builder = Cookie.Builder()
                .name(nameValue[0])
                .value(nameValue[1])
            
            parts.drop(1).forEach { part ->
                val keyValue = part.split("=", limit = 2)
                if (keyValue.size == 2) {
                    when (keyValue[0].trim().lowercase()) {
                        "domain" -> builder.domain(keyValue[1])
                        "path" -> builder.path(keyValue[1])
                        "expires" -> builder.expiresAt(keyValue[1].toLongOrNull() ?: 0L)
                        "secure" -> if (keyValue[1] == "true") builder.secure()
                        "httponly" -> if (keyValue[1] == "true") builder.httpOnly()
                    }
                }
            }
            
            builder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cookie: $cookieString", e)
            null
        }
    }
    
    /**
     * Clear all stored cookies
     */
    fun clearAllCookies() {
        Log.d(TAG, "Clearing all stored cookies")
        preferences.edit().clear().apply()
    }
    
    /**
     * Clear cookies for specific host
     */
    fun clearCookiesForHost(host: String) {
        Log.d(TAG, "Clearing cookies for host: $host")
        preferences.edit().remove(host).apply()
    }
}