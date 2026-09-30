package com.example.pelecarddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pelecarddemo.ui.navigation.PeleNavHost
import com.example.pelecarddemo.ui.theme.PeleDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeleDemoTheme {
                PeleNavHost()
            }
        }
    }
}
