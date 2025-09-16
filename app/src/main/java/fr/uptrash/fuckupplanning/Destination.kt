package fr.uptrash.fuckupplanning

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val icon: ImageVector,
    @param:StringRes val label: Int,
    val contentDescription: String
) {
    CALENDAR(
        route = "calendar",
        icon = Icons.Filled.CalendarToday,
        label = R.string.nav_calendar,
        contentDescription = "Calendar screen"
    ),
    HOMEWORK(
        route = "homework",
        icon = Icons.Filled.Book,
        label = R.string.nav_homework,
        contentDescription = "Homework screen"
    );
}