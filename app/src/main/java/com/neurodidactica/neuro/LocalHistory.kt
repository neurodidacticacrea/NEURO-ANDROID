package com.neurodidactica.neuro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalHistory {
    private const val FILE = "neuro_history"
    private const val KEY = "items"
    private const val MAX = 30

    fun add(context: Context, command: String) {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val old = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        val next = JSONArray()
        next.put(
            JSONObject()
                .put("command", command)
                .put("time", SimpleDateFormat("dd/MM HH:mm", Locale("es","MX")).format(Date()))
        )
        for (i in 0 until minOf(old.length(), MAX - 1)) next.put(old.getJSONObject(i))
        prefs.edit().putString(KEY, next.toString()).apply()
    }

    fun text(context: Context): String {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        if (arr.length() == 0) return "Aún no hay órdenes."
        val out = StringBuilder()
        for (i in 0 until arr.length()) {
            val x = arr.getJSONObject(i)
            out.append("• ").append(x.optString("command"))
                .append("  ·  ").append(x.optString("time")).append("\n")
        }
        return out.toString().trim()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
