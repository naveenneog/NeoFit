package com.neofit.core.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Applies and persists the app UI locale using AppCompat per-app languages.
 * This reliably switches Android string resources at runtime (and recreates
 * activities) across API levels, and survives restarts via the
 * AppLocalesMetadataHolderService (autoStoreLocales) declared in the manifest.
 */
object LocaleController {
    fun apply(localeTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    fun currentTag(): String =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "en" }
}
