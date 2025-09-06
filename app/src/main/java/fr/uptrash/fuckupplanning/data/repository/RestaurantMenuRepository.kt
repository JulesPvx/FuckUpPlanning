package fr.uptrash.fuckupplanning.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantMenuRepository @Inject constructor() {
    data class MenuItem(val name: String, val points: Int?)

    // Fetches the restaurant page and extracts the menu under ul.meal_foodies
    // Returns a map of category -> list of MenuItem
    suspend fun fetchMenu(): Map<String, List<MenuItem>> = withContext(Dispatchers.IO) {
        val url = "https://www.crous-poitiers.fr/restaurant/r-u-crousty/"
        val doc = Jsoup.connect(url).get()
        val menuMap = mutableMapOf<String, MutableList<MenuItem>>()

        val mealList = doc.selectFirst("ul.meal_foodies")
        if (mealList != null) {
            // iterate over top-level li elements (categories)
            val categories = mealList.select("> li")
            val pointsRegex =
                Regex("^(.*?)\\s*\\(\\s*(\\d+)\\s*points?\\s*\\)", RegexOption.IGNORE_CASE)
            for (cat in categories) {
                val categoryTitle = cat.ownText().trim().ifEmpty { "Menu" }
                val items = cat.select("ul > li").map { li ->
                    val raw = li.text().trim()
                    val match = pointsRegex.find(raw)
                    if (match != null) {
                        val name = match.groupValues[1].trim()
                        val points = match.groupValues[2].toIntOrNull()
                        MenuItem(name = name, points = points)
                    } else {
                        MenuItem(name = raw, points = null)
                    }
                }
                menuMap[categoryTitle] = items.toMutableList()
            }
        }

        menuMap
    }
}
