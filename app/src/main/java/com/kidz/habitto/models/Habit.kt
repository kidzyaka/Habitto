package com.kidz.habitto.models

import java.time.LocalDate

enum class HabitType {
    CONTINUOUS, // Days Since
    WEEKLY,     // Weekly Routine
    CHALLENGE   // Accountability Challenge
}

data class Habit(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val type: HabitType,
    val iconName: String, // Name of the system icon
    val iconColor: Int,   // Color as Int
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    
    // Continuous / Challenge specific
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null, // Only for Challenge
    
    // Weekly specific
    val activeDays: List<Int> = emptyList(), // 1=Mon, 2=Tue, ..., 7=Sun
    
    // Progress tracking
    val completedDates: List<LocalDate> = emptyList(),
    val lastResetDate: LocalDate? = null // For Continuous "Reset" logic
)
