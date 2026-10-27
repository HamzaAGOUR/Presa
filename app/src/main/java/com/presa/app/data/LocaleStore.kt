package com.presa.app.data

import android.annotation.SuppressLint
import com.presa.app.domain.model.AppLanguage

/**
 * Locale needs to be known synchronously in Activity.attachBaseContext, before
 * DataStore can be read. DataStore remains the source of truth; this tiny
 * SharedPreferences mirror is written at the same time as the language setting.
 */
object LocaleStore {
    private const val PREFS = "presa_locale"
    private const val KEY_LANGUAGE = "language"

    fun readTag(context: android.content.Context): String =
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.FRENCH.tag)
            ?: AppLanguage.FRENCH.tag

    // Deliberately synchronous: the value must be readable in
    // Activity.attachBaseContext before the process continues.
    @SuppressLint("ApplySharedPref")
    fun writeTag(context: android.content.Context, tag: String) {
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, tag)
            .commit()
    }

    fun wrap(context: android.content.Context): android.content.Context {
        val locale = java.util.Locale.forLanguageTag(readTag(context))
        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
