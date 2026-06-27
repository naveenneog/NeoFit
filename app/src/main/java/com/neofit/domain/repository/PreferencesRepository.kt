package com.neofit.domain.repository

import com.neofit.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    /** null = follow system. */
    fun observeDarkTheme(): Flow<Boolean?>
    suspend fun setDarkTheme(value: Boolean?)

    fun observeOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete(done: Boolean)
}
