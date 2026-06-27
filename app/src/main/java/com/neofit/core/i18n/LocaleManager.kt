package com.neofit.core.i18n

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies a saved UI locale by wrapping the base context. Used from
 * [com.neofit.MainActivity.attachBaseContext], which runs before Hilt injection,
 * so this is a stateless object backed by SharedPreferences (read synchronously).
 *
 * The reactive source of truth for in-app text (food names etc.) is the
 * DataStore-backed PreferencesRepository surfaced via `LocalAppLanguage`; this
 * mirror exists only to localise Android string resources at startup.
 */
object LocaleManager {
    private const val PREFS = "neofit_locale"
    private const val KEY_TAG = "locale_tag"

    fun persist(context: Context, localeTag: String) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TAG, localeTag)
            .apply()
    }

    fun currentTag(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, "en") ?: "en"

    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(currentTag(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
