package com.neofit.core.i18n

import androidx.compose.runtime.compositionLocalOf
import com.neofit.domain.model.AppLanguage

/**
 * Current in-app language, provided at the root and consumed by composables that
 * render native dish/exercise names. Defaults to English.
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
