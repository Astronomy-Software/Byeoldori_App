package com.example.byeoldori.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.alarmDataStore by preferencesDataStore(name = "plan_alarm_prefs")

@Singleton
class PlanDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun keyFor(planId: Long): Preferences.Key<Int> =
        intPreferencesKey("alarm_minutes_${planId}")

    fun minutesOf(planId: Long): Flow<Int> =
        context.alarmDataStore.data.map { prefs -> prefs[keyFor(planId)] ?: 60 }

    suspend fun setMinutesOf(planId: Long, minutes: Int) {
        context.alarmDataStore.edit { it[keyFor(planId)] = minutes }
    }
}