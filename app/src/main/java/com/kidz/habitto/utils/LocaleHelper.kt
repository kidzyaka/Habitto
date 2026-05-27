package com.kidz.habitto.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    fun setLocale(languageCode: String) {
        val appLocale: LocaleListCompat = if (languageCode == "system") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("habitto_settings", Context.MODE_PRIVATE)
        return prefs.getString("selected_language", "system") ?: "system"
    }

    fun persistLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("habitto_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", languageCode).apply()
        setLocale(languageCode)
    }
}
