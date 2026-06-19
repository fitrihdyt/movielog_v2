package com.fitrinurhidayat0078.movielog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fitrinurhidayat0078.movielog.navigation.SetupNavGraph
import com.fitrinurhidayat0078.movielog.ui.theme.MovieLogTheme
import com.fitrinurhidayat0078.movielog.util.SettingsDataStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dataStore = SettingsDataStore(this@MainActivity)
            val darkMode by dataStore.darkModeFlow.collectAsState(false)
            MovieLogTheme(
                darkTheme = darkMode
            ) {
                SetupNavGraph()
            }
        }
    }
}