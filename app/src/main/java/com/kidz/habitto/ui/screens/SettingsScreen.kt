package com.kidz.habitto.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidz.habitto.notifications.ReminderManager
import com.kidz.habitto.ui.theme.*
import com.kidz.habitto.viewmodel.HabitViewModel
import java.util.*

@Composable
fun SettingsScreen(viewModel: HabitViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("habitto_settings", android.content.Context.MODE_PRIVATE) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean("reminder_enabled", false)) }
    var reminderHour by remember { mutableIntStateOf(prefs.getInt("reminder_hour", 20)) }
    var reminderMinute by remember { mutableIntStateOf(prefs.getInt("reminder_minute", 0)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        // Notifications Section
        Text("Notifications", style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HabittoSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(HabittoPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = HabittoPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Daily Reminder", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Check your habits every day", style = MaterialTheme.typography.bodySmall, color = HabittoOnSurfaceSecondary)
                        }
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { 
                            reminderEnabled = it
                            prefs.edit().putBoolean("reminder_enabled", it).apply()
                            if (it) {
                                ReminderManager.scheduleReminder(context, reminderHour, reminderMinute)
                            } else {
                                ReminderManager.cancelReminder(context)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = HabittoPrimary, checkedTrackColor = HabittoPrimary.copy(alpha = 0.5f))
                    )
                }

                if (reminderEnabled) {
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsItem(
                        icon = Icons.Default.AccessTime,
                        title = "Reminder Time",
                        subtitle = String.format("%02d:%02d", reminderHour, reminderMinute),
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                reminderHour = h
                                reminderMinute = m
                                prefs.edit().putInt("reminder_hour", h).putInt("reminder_minute", m).apply()
                                ReminderManager.scheduleReminder(context, h, m)
                            }, reminderHour, reminderMinute, true).show()
                        }
                    )
                }
            }
        }

        // Data & About Section
        Text("App Info", style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HabittoSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "Local-only, no data leaves your device",
                    onClick = {}
                )
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0 (Disciplined Beta)",
                    onClick = {}
                )
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Irreversibly delete all habits",
                    iconColor = HabittoReset,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = HabittoSurface,
            title = { Text("Delete All Data?", color = Color.White) },
            text = { Text("This will permanently remove all your habits and progress. This action cannot be undone.", color = HabittoOnSurfaceSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showDeleteDialog = false
                }) {
                    Text("Delete Everything", color = HabittoReset)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = HabittoPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = HabittoOnSurfaceSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HabittoOnSurfaceSecondary)
    }
}
