package com.example.menuannam


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.menuannam.ui.theme.AppNavigation
import com.example.menuannam.ui.theme.MenuAnNamTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuAnNamTheme {
                // Gọi AppNavigation (File Navigator.kt sẽ lo phần điều hướng)
                AppNavigation()
            }
        }
    }
}