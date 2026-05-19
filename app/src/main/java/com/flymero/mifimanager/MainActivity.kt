package com.flymero.mifimanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.navigation.MiFiNavHost
import com.flymero.mifimanager.ui.theme.LocalThemeControl
import com.flymero.mifimanager.ui.theme.MiFiManagerTheme
import com.flymero.mifimanager.ui.theme.ThemeControl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataStore: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkMode by remember { mutableStateOf(dataStore.isDarkMode()) }
            val themeControl = ThemeControl(
                isDark = darkMode,
                toggle = {
                    darkMode = !darkMode
                    dataStore.setDarkMode(darkMode)
                }
            )
            CompositionLocalProvider(LocalThemeControl provides themeControl) {
                MiFiManagerTheme(darkTheme = darkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MiFiNavHost()
                    }
                }
            }
        }
    }
}
