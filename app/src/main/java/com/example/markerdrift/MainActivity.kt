package com.example.markerdrift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.markerdrift.ui.LocalPlaces
import com.example.markerdrift.ui.MapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val local = LocalPlaces.fromBuildConfig()
                if (local != null) {
                    MapScreen(places = local, initialTarget = local.first().position, fitToPlaces = true)
                } else {
                    MapScreen()
                }
            }
        }
    }
}
