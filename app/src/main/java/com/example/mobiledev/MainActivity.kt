package com.example.mobiledev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.mobiledev.navigation.NavGraph
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase safely
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Handle cases where google-services.json is missing or invalid
            e.printStackTrace()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MobileDevTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
