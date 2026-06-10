package com.kidz.habitto.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidz.habitto.R
import com.kidz.habitto.models.Habit
import com.kidz.habitto.models.HabitType
import com.kidz.habitto.ui.theme.*
import com.kidz.habitto.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, onHabitSaved: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HabitType.WEEKLY) }
    var selectedIcon by remember { mutableStateOf("book") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var activeDays by remember { mutableStateOf(setOf(2, 4)) } // T, T default from design

    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.build_routine),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = stringResource(R.string.build_routine_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = HabittoOnSurfaceSecondary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HabittoSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.what_to_track), style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text(stringResource(R.string.title_placeholder), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HabittoPrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                // Icon Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.icon), style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val icons = listOf(
                            "book" to Icons.AutoMirrored.Filled.MenuBook,
                            "water" to Icons.Default.WaterDrop,
                            "gym" to Icons.Default.FitnessCenter,
                            "health" to Icons.Default.MonitorHeart,
                            "meditate" to Icons.Default.SelfImprovement,
                            "sleep" to Icons.Default.NightsStay
                        )
                        icons.forEach { (name, vector) ->
                            val isSelected = selectedIcon == name
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) HabittoPrimary else HabittoSurfaceVariant)
                                    .clickable { selectedIcon = name },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Habit Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.habit_type), style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HabittoBackground, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        HabitType.entries.forEach { type ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HabittoSurfaceVariant else Color.Transparent)
                                    .clickable { selectedType = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(type) {
                                        HabitType.CONTINUOUS -> stringResource(R.string.type_continuous)
                                        HabitType.WEEKLY -> stringResource(R.string.type_weekly)
                                        HabitType.CHALLENGE -> stringResource(R.string.type_challenge)
                                    },
                                    color = if (isSelected) Color.White else HabittoOnSurfaceSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // Dynamic Fields
                when (selectedType) {
                    HabitType.WEEKLY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.active_days), style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                val days = java.time.DayOfWeek.entries.map { dayOfWeek ->
                                    dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.getDefault())
                                }
                                days.forEachIndexed { index, day ->
                                    val dayNum = index + 1
                                    val isSelected = activeDays.contains(dayNum)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) HabittoPrimary else HabittoSurfaceVariant)
                                            .clickable {
                                                activeDays = if (isSelected) activeDays - dayNum else activeDays + dayNum
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day,
                                            color = if (isSelected) Color.Black else HabittoOnSurfaceSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Info box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F2027).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = HabittoBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.weekly_goal_info, activeDays.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HabittoBlue
                                    )
                                }
                            }
                        }
                    }
                    HabitType.CONTINUOUS -> {
                        DateField(stringResource(R.string.start_date), startDate, dateFormatter) {
                            DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
                        }
                    }
                    HabitType.CHALLENGE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DateField(stringResource(R.string.start_date), startDate, dateFormatter) {
                                DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
                            }
                            DateField(stringResource(R.string.end_date), endDate, dateFormatter) {
                                DatePickerDialog(context, { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) }, endDate.year, endDate.monthValue - 1, endDate.dayOfMonth).show()
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val habit = Habit(
                        title = title,
                        type = selectedType,
                        iconName = selectedIcon,
                        iconColor = HabittoPrimary.toArgb(),
                        startDate = startDate,
                        endDate = if (selectedType == HabitType.CHALLENGE) endDate else null,
                        activeDays = if (selectedType == HabitType.WEEKLY) activeDays.toList() else emptyList()
                    )
                    viewModel.addHabit(habit)
                    onHabitSaved()
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabittoPrimary,
                disabledContainerColor = HabittoSurfaceVariant
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_habit), color = Color.Black, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun DateField(label: String, date: LocalDate, formatter: DateTimeFormatter, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = HabittoOnSurfaceSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = date.format(formatter), color = Color.White)
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = HabittoOnSurfaceSecondary, modifier = Modifier.size(20.dp))
            }
        }
        HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
    }
}

fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
           ((this.red * 255.0f + 0.5f).toInt() shl 16) or
           ((this.green * 255.0f + 0.5f).toInt() shl 8) or
           (this.blue * 255.0f + 0.5f).toInt()
}
