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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(application)

    init {
        checkAndArchiveFailedChallenges()
    }

    val activeHabits: StateFlow<List<Habit>> = repository.habits
        .map { list -> list.filter { !it.isArchived } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun checkAndArchiveFailedChallenges() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val currentHabits = repository.habits.value
            
            val toArchive = currentHabits.filter { habit ->
                if (habit.isArchived) return@filter false
                
                // Challenge Failure Logic: If a day was missed, move to archive
                if (habit.type == com.kidz.habitto.models.HabitType.CHALLENGE) {
                    val start = habit.startDate ?: today
                    // If challenge has started and yesterday was not completed (and it wasn't the first day)
                    if (today.isAfter(start)) {
                        val wasYesterdayCompleted = habit.completedDates.contains(yesterday)
                        val wasYesterdayBeforeStart = yesterday.isBefore(start)
                        
                        if (!wasYesterdayCompleted && !wasYesterdayBeforeStart) return@filter true
                    }
                    
                    // Also archive if the end date has passed
                    if (habit.endDate != null && today.isAfter(habit.endDate)) return@filter true
                }
                false
            }.map { it.copy(isArchived = true, isPinned = false) }

            if (toArchive.isNotEmpty()) {
                repository.updateHabits(toArchive)
            }
        }
    }

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
