package com.neofit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.core.designsystem.NeoFitTheme
import com.neofit.core.i18n.LocalAppLanguage
import com.neofit.feature.AppViewModel
import com.neofit.feature.navigation.NeoFitNavGraph
import com.neofit.feature.onboarding.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: AppViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val dark = state.darkTheme ?: isSystemInDarkTheme()

            NeoFitTheme(darkTheme = dark) {
                CompositionLocalProvider(LocalAppLanguage provides state.language) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (state.loading) {
                            SplashScreen()
                        } else {
                            NeoFitNavGraph(startDestination = state.startDestination)
                        }
                    }
                }
            }
        }
    }
}
