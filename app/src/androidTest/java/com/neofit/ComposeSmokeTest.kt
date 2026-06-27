package com.neofit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.neofit.core.designsystem.NeoFitTheme
import com.neofit.feature.onboarding.SplashScreen
import org.junit.Rule
import org.junit.Test

/** Smoke test: the splash renders the brand name through Compose + resources. */
class ComposeSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splash_showsAppName() {
        composeRule.setContent {
            NeoFitTheme { SplashScreen() }
        }
        composeRule.onNodeWithText("Neo Fit").assertIsDisplayed()
    }
}
