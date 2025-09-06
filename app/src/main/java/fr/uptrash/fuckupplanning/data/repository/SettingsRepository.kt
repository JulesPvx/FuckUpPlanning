package fr.uptrash.fuckupplanning.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class TPGroup {
    ALL,
    TP1,
    TP2,
    TP3,
    TP4
}

interface SettingsRepository {
    val selectedTPGroupFlow: Flow<TPGroup>
    suspend fun saveSelectedTPGroup(tpGroup: TPGroup)
}

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val KEY_SELECTED_TP = stringPreferencesKey("selected_tp_group")
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

    override suspend fun saveSelectedTPGroup(tpGroup: TPGroup) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_TP] = tpGroup.name
        }
    }
}

