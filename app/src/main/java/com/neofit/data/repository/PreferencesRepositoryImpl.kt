package com.neofit.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.neoDataStore by preferencesDataStore(name = "neofit_prefs")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : PreferencesRepository {

    private val store = context.neoDataStore

    override fun observeLanguage(): Flow<AppLanguage> =
        store.data.map { AppLanguage.fromCode(it[KEY_LANGUAGE]) }

    override suspend fun setLanguage(language: AppLanguage) {
        store.edit { it[KEY_LANGUAGE] = language.code }
    }

    override fun observeDarkTheme(): Flow<Boolean?> =
        store.data.map { it[KEY_DARK_THEME] }

    override suspend fun setDarkTheme(value: Boolean?) {
        store.edit { prefs ->
            if (value == null) prefs.remove(KEY_DARK_THEME) else prefs[KEY_DARK_THEME] = value
        }
    }

    override fun observeOnboardingComplete(): Flow<Boolean> =
        store.data.map { it[KEY_ONBOARDING] ?: false }

    override suspend fun setOnboardingComplete(done: Boolean) {
        store.edit { it[KEY_ONBOARDING] = done }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
    }
}
