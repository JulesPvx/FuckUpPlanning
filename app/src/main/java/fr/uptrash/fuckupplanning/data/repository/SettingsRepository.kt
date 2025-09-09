package fr.uptrash.fuckupplanning.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.uptrash.fuckupplanning.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class TPGroup {
    ALL,
    TP1,
    TP2,
    TP3,
    TP4
}

enum class MMIYear {
    MMI1,
    MMI2,
    MMI3
}

interface SettingsRepository {
    val selectedTPGroupFlow: Flow<TPGroup>
    val selectedMMIYearFlow: Flow<MMIYear>
    val selectedAppThemeFlow: Flow<AppTheme>
    suspend fun saveSelectedTPGroup(tpGroup: TPGroup)
    suspend fun saveSelectedMMIYear(mmiYear: MMIYear)
    suspend fun saveSelectedAppTheme(appTheme: AppTheme)
}

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val KEY_SELECTED_TP = stringPreferencesKey("selected_tp_group")
        val KEY_SELECTED_MMI_YEAR = stringPreferencesKey("selected_mmi_year")
        val KEY_SELECTED_APP_THEME = stringPreferencesKey("selected_app_theme")
    }

    override val selectedTPGroupFlow: Flow<TPGroup> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_TP] ?: TPGroup.ALL.name
            try {
                TPGroup.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                TPGroup.ALL
            }
        }

    override val selectedMMIYearFlow: Flow<MMIYear> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_MMI_YEAR] ?: MMIYear.MMI2.name
            try {
                MMIYear.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                MMIYear.MMI2
            }
        }

    override val selectedAppThemeFlow: Flow<AppTheme> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_APP_THEME] ?: AppTheme.SYSTEM.key
            try {
                AppTheme.fromKey(stored)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    override suspend fun saveSelectedTPGroup(tpGroup: TPGroup) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_TP] = tpGroup.name
        }
    }

    override suspend fun saveSelectedMMIYear(mmiYear: MMIYear) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_MMI_YEAR] = mmiYear.name
        }
    }

    override suspend fun saveSelectedAppTheme(appTheme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_APP_THEME] = appTheme.key
        }
    }
}
