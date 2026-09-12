package com.neurodidactica.neuro

import android.content.Context

object SecurityPreferences {
    private const val FILE = "neuro_security"
    private const val KEY_PRIVATE_MODE = "private_mode"
    private const val KEY_ALWAYS_AVAILABLE = "always_available"
    private const val KEY_WAKE_WORD = "wake_word_enabled"

    fun isPrivateMode(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_PRIVATE_MODE, true)

    fun setPrivateMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PRIVATE_MODE, enabled).apply()
    }

    fun isAlwaysAvailable(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALWAYS_AVAILABLE, false)

    fun setAlwaysAvailable(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ALWAYS_AVAILABLE, enabled).apply()
    }

    fun isWakeWordEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_WAKE_WORD, false)

    fun setWakeWordEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
    }
}
