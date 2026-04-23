package com.kntransport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.navigation.compose.rememberNavController
import com.kntransport.app.navigation.KntNavHost
import com.kntransport.app.network.ApiClient
import com.kntransport.app.ui.theme.KNTTransportTheme
import com.kntransport.app.ui.theme.LocalThemeModeToggle
import com.kntransport.app.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val android.content.Context.dataStore by preferencesDataStore(name = "knt_prefs")
private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check first-launch onboarding flag (blocking read — only on startup)
        val onboardingDone = runBlocking {
            dataStore.data.first()[KEY_ONBOARDING_DONE] ?: false
        }

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.DARK) }
            val systemIsDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> systemIsDark
            }

            CompositionLocalProvider(LocalThemeModeToggle provides { themeMode = it }) {
                KNTTransportTheme(isDark = isDark, themeMode = themeMode) {
                    val navController = rememberNavController()
                    KntNavHost(
                        navController    = navController,
                        showOnboarding   = !onboardingDone,
                        onOnboardingDone = {
                            CoroutineScope(Dispatchers.IO).launch {
                                dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = true }
                            }
                        },
                    )
                }
            }
        }
    }
}
