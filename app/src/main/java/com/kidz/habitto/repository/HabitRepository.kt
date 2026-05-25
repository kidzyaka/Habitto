package com.kidz.habitto.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.*
import com.kidz.habitto.models.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("habitto_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    private val _habits = MutableStateFlow<List<Habit>>(loadHabits())
    val habits: StateFlow<List<Habit>> = _habits

    private fun loadHabits(): List<Habit> {
        val json = prefs.getString("habits", null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<Habit>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHabits(list: List<Habit>) {
        val json = gson.toJson(list)
        prefs.edit().putString("habits", json).apply()
        _habits.value = list
    }

    fun addHabit(habit: Habit) {
        saveHabits(_habits.value + habit)
    }

    fun updateHabit(habit: Habit) {
        saveHabits(_habits.value.map { if (it.id == habit.id) habit else it })
    }

    fun deleteHabit(id: Long) {
        saveHabits(_habits.value.filter { it.id != id })
    }

    private class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.format(formatter))
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate {
            return LocalDate.parse(json.asString, formatter)
        }
    }
}
