package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.CameraSimulatorScreen
import com.example.ui.CameraViewModel
import com.example.ui.CameraViewModelFactory
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    Dashboard,
    CameraSimulator
}

class MainActivity : ComponentActivity() {
    private val viewModel: CameraViewModel by viewModels {
        CameraViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_routing"
                    ) { screen ->
                        when (screen) {
                            AppScreen.Dashboard -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onLaunchSimulator = { currentScreen = AppScreen.CameraSimulator },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            AppScreen.CameraSimulator -> {
                                CameraSimulatorScreen(
                                    viewModel = viewModel,
                                    onBackToDashboard = { currentScreen = AppScreen.Dashboard },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

