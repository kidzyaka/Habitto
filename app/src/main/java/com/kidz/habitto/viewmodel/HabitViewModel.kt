package com.kidz.habitto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kidz.habitto.models.Habit
import com.kidz.habitto.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(application)

    val activeHabits: StateFlow<List<Habit>> = repository.habits
        .map { list -> 
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            
            val processedList = list.map { habit ->
                if (habit.isArchived) return@map habit
                
                // Challenge Failure Logic: If a day was missed, move to archive
                if (habit.type == com.kidz.habitto.models.HabitType.CHALLENGE) {
                    val start = habit.startDate ?: today
                    // If challenge has started and yesterday was not completed (and it wasn't the first day)
                    if (today.isAfter(start)) {
                        val wasYesterdayCompleted = habit.completedDates.contains(yesterday)
                        val wasYesterdayBeforeStart = yesterday.isBefore(start)
                        
                        if (!wasYesterdayCompleted && !wasYesterdayBeforeStart) {
                            return@map habit.copy(isArchived = true, isPinned = false)
                        }
                    }
                    
                    // Also archive if the end date has passed
                    if (habit.endDate != null && today.isAfter(habit.endDate)) {
                        return@map habit.copy(isArchived = true, isPinned = false)
                    }
                }
                habit
            }
            
            // Sync any automated archiving back to repository if list changed
            if (processedList != list) {
                processedList.forEach { habit ->
                    val original = list.find { it.id == habit.id }
                    if (original != null && original.isArchived != habit.isArchived) {
                        repository.updateHabit(habit)
                    }
                }
            }
            
            processedList.filter { !it.isArchived }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedHabits: StateFlow<List<Habit>> = repository.habits
        .map { list -> list.filter { it.isArchived } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(habit: Habit) {
        repository.addHabit(habit)
    }

    fun updateHabit(habit: Habit) {
        repository.updateHabit(habit)
    }

    fun archiveHabit(habitId: Long) {
        val habit = repository.habits.value.find { it.id == habitId }
        habit?.let {
            updateHabit(it.copy(isArchived = true, isPinned = false))
        }
    }

    fun unarchiveHabit(habitId: Long) {
        val habit = repository.habits.value.find { it.id == habitId }
        habit?.let {
            updateHabit(it.copy(isArchived = false))
        }
    }

    fun deleteHabit(habitId: Long) {
        repository.deleteHabit(habitId)
    }

    fun clearAllData() {
        val allIds = repository.habits.value.map { it.id }
        allIds.forEach { repository.deleteHabit(it) }
    }

    fun toggleCompletion(habit: Habit, date: LocalDate) {
        val newCompletedDates = if (habit.completedDates.contains(date)) {
            habit.completedDates.filter { it != date }
        } else {
            habit.completedDates + date
        }
        updateHabit(habit.copy(completedDates = newCompletedDates))
    }

    fun resetHabit(habit: Habit) {
        updateHabit(habit.copy(lastResetDate = LocalDate.now(), completedDates = emptyList()))
    }
}
