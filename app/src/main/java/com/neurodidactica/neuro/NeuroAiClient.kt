package com.neurodidactica.neuro

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object NeuroAiClient {

    /**
     * IMPORTANT:
     * Never put an OpenAI API key in the Android app.
     * Point BuildConfig.NEURO_API_URL to your own secure backend
     * (Render, Vercel Functions, Python/FastAPI, etc.).
     */
    fun ask(message: String, callback: (String?) -> Unit) {
        if (BuildConfig.NEURO_API_URL.contains("example.com")) {
            callback(null)
            return
        }

        thread {
            try {
                val conn = URL(BuildConfig.NEURO_API_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 12000
                conn.readTimeout = 30000
                conn.doOutput = true

                val payload = JSONObject()
                    .put("message", message)
                    .put("assistant", "NEURO")
                    .toString()

                OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val reply = JSONObject(body).optString("reply", null)
                callback(reply)
                conn.disconnect()
            } catch (_: Exception) {
                callback(null)
            }
        }
    }
}
