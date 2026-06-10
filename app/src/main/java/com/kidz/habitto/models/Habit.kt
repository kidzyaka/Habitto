package com.kidz.habitto.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

enum class HabitType {
    CONTINUOUS, // Days Since
    WEEKLY,     // Weekly Routine
    CHALLENGE   // Accountability Challenge
}

data class Habit(
    @SerializedName("id") val id: Long = System.currentTimeMillis(),
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: HabitType,
    @SerializedName("iconName") val iconName: String, // Name of the system icon
    @SerializedName("iconColor") val iconColor: Int,   // Color as Int
    @SerializedName("isPinned") val isPinned: Boolean = false,
    @SerializedName("isArchived") val isArchived: Boolean = false,
    
    // Continuous / Challenge specific
    @SerializedName("startDate") val startDate: LocalDate? = null,
    @SerializedName("endDate") val endDate: LocalDate? = null, // Only for Challenge
    
    // Weekly specific
    @SerializedName("activeDays") val activeDays: List<Int> = emptyList(), // 1=Mon, 2=Tue, ..., 7=Sun
    
    // Progress tracking
    @SerializedName("completedDates") val completedDates: List<LocalDate> = emptyList(),
    @SerializedName("lastResetDate") val lastResetDate: LocalDate? = null // For Continuous "Reset" logic
)
