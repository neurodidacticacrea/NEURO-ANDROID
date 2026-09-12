package com.neurodidactica.neuro

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var response: TextView
    private lateinit var commandInput: EditText
    private lateinit var router: NeuroCommandRouter

    private val voiceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spoken = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    .orEmpty()
                if (spoken.isNotBlank()) {
                    commandInput.setText(spoken)
                    runCommand(spoken)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router = NeuroCommandRouter(this) { text ->
            runOnUiThread {
                response.text = text
                NeuroSpeech.say(this, text)
            }
        }
        setContentView(buildUi())
        requestRuntimePermissions()
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            setBackgroundColor(Color.rgb(7, 2, 13))
        }

        val title = TextView(this).apply {
            text = "NEURO"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val subtitle = TextView(this).apply {
            text = "Inteligencia personal · Neurodidáctica"
            textSize = 14f
            setTextColor(Color.rgb(180, 145, 220))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        root.addView(title)
        root.addView(subtitle)

        val brain = ImageView(this).apply {
            setImageResource(com.neurodidactica.neuro.R.drawable.neuro_brain)
            adjustViewBounds = true
            setPadding(24, 26, 24, 12)
            contentDescription = "NEURO"
            animate().scaleX(1.06f).scaleY(1.06f).setDuration(900).withEndAction {
                animate().scaleX(1f).scaleY(1f).setDuration(900).withEndAction {
                    post { buildPulse(this) }
                }.start()
            }.start()
            setOnClickListener { listen() }
        }
        root.addView(brain, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 360
        ))

        status = TextView(this).apply {
            text = "● LISTO"
            textSize = 13f
            setTextColor(Color.rgb(58, 215, 255))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        root.addView(status)

        commandInput = EditText(this).apply {
            hint = "Escribe o toca el cerebro y habla…"
            setHintTextColor(Color.rgb(130, 110, 150))
            setTextColor(Color.WHITE)
            setSingleLine(false)
            setPadding(22, 18, 22, 18)
            setBackgroundColor(Color.rgb(24, 10, 38))
        }
        root.addView(commandInput, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 18 })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val talk = Button(this).apply {
            text = "🎙 Hablar"
            setOnClickListener { listen() }
        }
        val run = Button(this).apply {
            text = "Ejecutar"
            setOnClickListener { runCommand(commandInput.text.toString()) }
        }
        val floatBtn = Button(this).apply {
            text = "🧠 Flotante"
            setOnClickListener { enableFloating() }
        }
        row.addView(talk)
        row.addView(run)
        row.addView(floatBtn)
        root.addView(row)

        response = TextView(this).apply {
            text = "Bienvenido. Soy NEURO, tu inteligencia personal."
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(18, 24, 18, 12)
        }
        root.addView(response)

        val hints = TextView(this).apply {
            text = """
                Prueba:
                • “Abre Spotify”
                • “Abre WhatsApp”
                • “Pon una alarma a las 7”
                • “Llama al 4421234567”
                • “Abre Maps”
                • “Crea un plan para mañana”
                • “Muéstrame mis fotos”

                Acciones sensibles pedirán confirmación antes de ejecutarse.
            """.trimIndent()
            setTextColor(Color.rgb(190, 175, 210))
            textSize = 13f
        }
        root.addView(hints)

        return ScrollView(this).apply { addView(root) }
    }

    private fun buildPulse(view: View) {
        view.animate().scaleX(1.05f).scaleY(1.05f).alpha(0.96f).setDuration(1200).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(1200).withEndAction {
                view.post { buildPulse(view) }
            }.start()
        }.start()
    }

    private fun listen() {
        status.text = "● ESCUCHANDO"
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con NEURO")
        }
        voiceLauncher.launch(intent)
    }

    private fun runCommand(text: String) {
        if (text.isBlank()) return
        status.text = "● PENSANDO"
        router.handle(text)
        status.postDelayed({ status.text = "● LISTO" }, 1200)
    }

    private fun enableFloating() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ))
            Toast.makeText(this, "Activa “Mostrar sobre otras apps” y vuelve a NEURO.", Toast.LENGTH_LONG).show()
            return
        }
        ContextCompat.startForegroundService(
            this,
            Intent(this, FloatingNeuroService::class.java)
        )
        Toast.makeText(this, "NEURO flotante activado", Toast.LENGTH_SHORT).show()
    }

    private fun requestRuntimePermissions() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            perms += Manifest.permission.RECORD_AUDIO
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            perms += Manifest.permission.POST_NOTIFICATIONS
        if (perms.isNotEmpty()) {
            requestPermissions(perms.toTypedArray(), 100)
        }
    }
}
