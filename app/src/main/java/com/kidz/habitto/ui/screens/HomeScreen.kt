package com.kidz.habitto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.habitto.models.Habit
import com.kidz.habitto.models.HabitType
import com.kidz.habitto.ui.theme.*
import com.kidz.habitto.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(viewModel: HabitViewModel, onAddHabit: () -> Unit) {
    val habits by viewModel.activeHabits.collectAsState()
    val pinnedHabit = habits.find { it.isPinned }
    val otherHabits = habits.filter { !it.isPinned }
    
    var habitToReset by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = HabittoPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
            ) {
                pinnedHabit?.let {
                    item {
                        PinnedHabitCard(it, onResetRequest = { habitToReset = it })
                    }
                }
                
                items(otherHabits) { habit ->
                    HabitListItem(
                        habit = habit,
                        onToggleCompletion = { date -> viewModel.toggleCompletion(habit, date) },
                        onArchive = { viewModel.archiveHabit(habit.id) },
                        onResetRequest = { habitToReset = habit }
                    )
                }
            }
        }
    }

    if (habitToReset != null) {
        AlertDialog(
            onDismissRequest = { habitToReset = null },
            containerColor = HabittoSurface,
            title = { Text("Reset Habit?", color = Color.White) },
            text = { Text("This will return the day counter to zero. This action cannot be undone.", color = HabittoOnSurfaceSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    habitToReset?.let { viewModel.resetHabit(it) }
                    habitToReset = null
                }) {
                    Text("Reset", color = HabittoReset)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToReset = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun PinnedHabitCard(habit: Habit, onResetRequest: () -> Unit) {
    val daysSince = ChronoUnit.DAYS.between(habit.lastResetDate ?: habit.startDate ?: LocalDate.now(), LocalDate.now())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HabittoSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = HabittoOnSurfaceSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pinned Habit",
                        style = MaterialTheme.typography.labelMedium,
                        color = HabittoOnSurfaceSecondary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = daysSince.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HabittoPrimary
                    )
                )
                Text(
                    text = "DAYS SINCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = HabittoOnSurfaceSecondary
                )
            }

            if (habit.type == HabitType.CONTINUOUS) {
                Button(
                    onClick = onResetRequest,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    colors = ButtonDefaults.buttonColors(containerColor = HabittoSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(18.dp),
                            tint = HabittoReset
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Reset", color = HabittoReset)
                    }
                }
            }
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    onToggleCompletion: (LocalDate) -> Unit,
    onArchive: () -> Unit,
    onResetRequest: () -> Unit
) {
    val today = LocalDate.now()
    val isCompletedToday = habit.completedDates.contains(today)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HabittoSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HabittoPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(habit.type) {
                            HabitType.WEEKLY -> Icons.Default.FitnessCenter
                            HabitType.CHALLENGE -> Icons.Default.EmojiEvents
                            else -> Icons.Default.Language
                        },
                        contentDescription = null,
                        tint = HabittoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f) ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = when(habit.type) {
                            HabitType.CONTINUOUS -> {
                                val days = ChronoUnit.DAYS.between(habit.lastResetDate ?: habit.startDate ?: today, today)
                                "$days Days Since"
                            }
                            HabitType.WEEKLY -> "Weekly goal: ${habit.activeDays.size} days"
                            HabitType.CHALLENGE -> {
                                val remaining = ChronoUnit.DAYS.between(today, habit.endDate ?: today)
                                if (remaining < 0) "Challenge Ended" else "$remaining Days Remaining"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = HabittoOnSurfaceSecondary
                    )
                }

                // Interaction Logic
                if (habit.type == HabitType.CONTINUOUS) {
                    IconButton(onClick = onResetRequest) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = HabittoReset)
                    }
                } else if (habit.type == HabitType.CHALLENGE) {
                    IconButton(
                        onClick = { onToggleCompletion(today) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCompletedToday) HabittoPrimary else HabittoSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = if (isCompletedToday) Color.Black else HabittoOnSurfaceSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Archive",
                        tint = HabittoOnSurfaceSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            when (habit.type) {
                HabitType.WEEKLY -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        days.forEachIndexed { index, day ->
                            val dayNum = index + 1
                            val isTargetDay = habit.activeDays.contains(dayNum)
                            
                            val firstDayOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                            val thisDay = firstDayOfWeek.plusDays(index.toLong())
                            val isCompleted = habit.completedDates.contains(thisDay)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> HabittoPrimary.copy(alpha = 0.6f)
                                            isTargetDay -> HabittoSurfaceVariant
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onToggleCompletion(thisDay) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (isCompleted) Color.Black else if (isTargetDay) HabittoOnSurfaceSecondary else Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                HabitType.CHALLENGE -> {
                    val start = habit.startDate ?: today
                    val end = habit.endDate ?: today.plusDays(1)
                    val totalDays = ChronoUnit.DAYS.between(start, end).toFloat().coerceAtLeast(1f)
                    val completionsInChallenge = habit.completedDates.count { (it.isEqual(start) || it.isAfter(start)) && (it.isEqual(end) || it.isBefore(end)) }
                    val progress = (completionsInChallenge / totalDays).coerceIn(0f, 1f)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Progress: $completionsInChallenge days", style = MaterialTheme.typography.labelSmall, color = HabittoYellow)
                        Text(text = "Goal: ${totalDays.toInt()} days", style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = HabittoYellow,
                        trackColor = HabittoSurfaceVariant
                    )
                }
                HabitType.CONTINUOUS -> {}
            }
        }
    }
}
