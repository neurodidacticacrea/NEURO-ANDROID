package com.neurodidactica.neuro

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class WakeWordService : Service(), RecognitionListener {

    companion object {
        const val ACTION_STOP = "com.neurodidactica.neuro.STOP_WAKEWORD"
        private const val CHANNEL = "neuro_wakeword"
        private const val NOTIFICATION_ID = 88
    }

    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return
        }

        recognizer = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
        } else {
            // Security-first policy: do not silently fall back to cloud recognition.
            null
        }

        if (recognizer == null) {
            NeuroSpeech.say(
                this,
                "La detección local de la palabra NEURO no está disponible en este dispositivo."
            )
            stopSelf()
            return
        }

        recognizer?.setRecognitionListener(this)
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            SecurityPreferences.setWakeWordEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startListening() {
        if (listening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
        runCatching {
            listening = true
            recognizer?.startListening(intent)
        }.onFailure {
            listening = false
            restartSoon()
        }
    }

    private fun handleTexts(texts: List<String>?) {
        if (texts.isNullOrEmpty()) return
        val heard = texts.joinToString(" ").lowercase(Locale("es", "MX"))
        if (Regex("""\bneuro\b""").containsMatchIn(heard)) {
            wakeNeuro()
        }
    }

    private fun wakeNeuro() {
        // Show the floating brain.
        ContextCompat.startForegroundService(
            this,
            Intent(this, FloatingNeuroService::class.java)
        )

        // Open NEURO and start listening for the next command.
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        launch?.putExtra("AUTO_LISTEN", true)
        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launch != null) startActivity(launch)

        NeuroSpeech.say(this, "Sí.")
    }

    private fun restartSoon() {
        listening = false
        android.os.Handler(mainLooper).postDelayed({
            if (SecurityPreferences.isWakeWordEnabled(this)) startListening()
        }, 650)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { listening = false }

    override fun onError(error: Int) {
        listening = false
        restartSoon()
    }

    override fun onResults(results: Bundle?) {
        listening = false
        handleTexts(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
        restartSoon()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        handleTexts(partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onDestroy() {
        runCatching { recognizer?.cancel() }
        runCatching { recognizer?.destroy() }
        recognizer = null
        listening = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                CHANNEL,
                "NEURO palabra de activación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "NEURO escucha localmente la palabra de activación."
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(c)
        }
    }

    private fun buildNotification(): Notification {
        val stop = Intent(this, WakeWordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 2, stop,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("NEURO está escuchando")
            .setContentText("Di “NEURO” para activar el cerebro flotante.")
            .setOngoing(true)
            .addAction(0, "Detener escucha", stopPending)
            .build()
    }
}
