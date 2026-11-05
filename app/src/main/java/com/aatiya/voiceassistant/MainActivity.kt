package com.aatiya.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aatiya.voiceassistant.ui.navigation.NavGraph
import com.aatiya.voiceassistant.ui.theme.AatiyaAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as AatiyaApplication).container

        setContent {
            AatiyaAITheme {
                NavGraph(container = appContainer)
            }
        }
    }
}
