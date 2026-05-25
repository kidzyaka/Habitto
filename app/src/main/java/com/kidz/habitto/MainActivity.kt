package com.kidz.habitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kidz.habitto.ui.screens.AddHabitScreen
import com.kidz.habitto.ui.screens.ArchiveScreen
import com.kidz.habitto.ui.screens.HomeScreen
import com.kidz.habitto.ui.screens.SettingsScreen
import com.kidz.habitto.ui.theme.*
import com.kidz.habitto.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabittoTheme {
                HabittoApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Archive : Screen("archive", "Archive", Icons.Default.Inventory2)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Add : Screen("add", "Add", Icons.Default.Add)
}

@Composable
fun HabittoApp() {
    val navController = rememberNavController()
    val viewModel: HabitViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = HabittoBackground,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                val screens = listOf(Screen.Home, Screen.Archive, Screen.Settings)
                screens.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = null,
                                tint = if (isSelected) HabittoPrimary else HabittoOnSurfaceSecondary
                            )
                        },
                        label = { Text(screen.label, color = if (isSelected) HabittoPrimary else HabittoOnSurfaceSecondary) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = HabittoBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(viewModel) {
                    navController.navigate(Screen.Add.route)
                } 
            }
            composable(Screen.Add.route) { 
                AddHabitScreen(viewModel) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Archive.route) { ArchiveScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}
