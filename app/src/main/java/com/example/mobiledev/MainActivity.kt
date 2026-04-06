package com.example.mobiledev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mobiledev.feature.signup.presentation.SignUpRoute
import com.example.mobiledev.feature.signup.presentation.SignUpViewModel
import com.example.mobiledev.ui.theme.MobileDevTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileDevTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SignUpRoute(viewModel = viewModel)
                }
            }
        }
    }
}

