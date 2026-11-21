package com.modutrap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single, process-wide DataStore backing all persisted app settings.
 *
 * The delegate is a top-level extension so the same DataStore instance is
 * returned for the same Context no matter which repository asks for it.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "modutrap_settings"
)

object SettingsDataStore {
    fun create(context: Context): DataStore<Preferences> = context.settingsDataStore
}
