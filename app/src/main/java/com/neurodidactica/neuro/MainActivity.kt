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
        if (intent?.getBooleanExtra("AUTO_LISTEN", false) == true) {
            status.postDelayed({ listen() }, 500)
        }
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
            text = "🧠 Activar flotante"
            setOnClickListener { enableFloating() }
        }
        val stopFloatBtn = Button(this).apply {
            text = "✕ Detener"
            setOnClickListener { stopFloating() }
        }
        row.addView(talk)
        row.addView(run)
        row.addView(floatBtn)
        row.addView(stopFloatBtn)
        root.addView(row)

        response = TextView(this).apply {
            text = "Bienvenido. Soy NEURO, tu inteligencia personal."
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(18, 24, 18, 12)
        }
        root.addView(response)


        val quickTitle = TextView(this).apply {
            text = "ACCIONES RÁPIDAS"
            textSize = 14f
            setTextColor(Color.rgb(58, 215, 255))
            setPadding(0, 24, 0, 8)
        }
        root.addView(quickTitle)

        val quickRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            "🎵 Música" to "abre Spotify",
            "🗺️ Mapas" to "abre Maps",
            "📅 Agenda" to "abre calendario"
        ).forEach { (label, cmd) ->
            quickRow1.addView(Button(this).apply {
                text = label
                setOnClickListener { runCommand(cmd) }
            })
        }
        root.addView(quickRow1)

        val quickRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(
            "☀️ Mañana" to "buenos días",
            "🚗 Conducir" to "modo conducción",
            "🌙 Dormir" to "buenas noches"
        ).forEach { (label, cmd) ->
            quickRow2.addView(Button(this).apply {
                text = label
                setOnClickListener { runCommand(cmd) }
            })
        }
        root.addView(quickRow2)

        val historyBtn = Button(this).apply {
            text = "🧾 Ver historial local"
            setOnClickListener {
                response.text = LocalHistory.text(this@MainActivity)
            }
        }
        root.addView(historyBtn)

        val updateBtn = Button(this).apply {
            text = "⬆ Centro de actualizaciones"
            setOnClickListener { UpdateCenter.open(this@MainActivity) }
        }
        root.addView(updateBtn)

        val securityTitle = TextView(this).apply {
            text = "SEGURIDAD Y PRIVACIDAD"
            textSize = 14f
            setTextColor(Color.rgb(228, 60, 255))
            setPadding(0, 28, 0, 8)
        }
        root.addView(securityTitle)

        val privateMode = Switch(this).apply {
            text = "Modo privado (no enviar conversación a Internet)"
            setTextColor(Color.WHITE)
            isChecked = SecurityPreferences.isPrivateMode(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                SecurityPreferences.setPrivateMode(this@MainActivity, checked)
                Toast.makeText(
                    this@MainActivity,
                    if (checked) "Modo privado activado" else "IA en línea permitida",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        root.addView(privateMode)

        val alwaysAvailable = Switch(this).apply {
            text = "Siempre disponible después de reiniciar"
            setTextColor(Color.WHITE)
            isChecked = SecurityPreferences.isAlwaysAvailable(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                SecurityPreferences.setAlwaysAvailable(this@MainActivity, checked)
                Toast.makeText(
                    this@MainActivity,
                    if (checked) "Persistencia 24/7 autorizada" else "Persistencia desactivada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        root.addView(alwaysAvailable)

        val securityInfo = TextView(this).apply {
            text = "NEURO no usa Accesibilidad ni lee el contenido de otras apps. Si el cerebro no aparece, verifica que “Mostrar sobre otras apps” esté activado. Al iniciar correctamente verás una notificación permanente: “NEURO flotante activo”."
            setTextColor(Color.rgb(190, 175, 210))
            textSize = 12f
            setPadding(0, 8, 0, 8)
        }
        root.addView(securityInfo)

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
            Toast.makeText(
                this,
                "Primero activa “Permitir mostrar sobre otras apps” para NEURO.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        val serviceIntent = Intent(this, FloatingNeuroService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        status.text = "● FLOTANTE ACTIVO"
        response.text = "El cerebro flotante de NEURO está activo. Busca el cerebro en el lado superior izquierdo de la pantalla."
        Toast.makeText(
            this,
            "NEURO flotante activado",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun stopFloating() {
        stopService(Intent(this, FloatingNeuroService::class.java))
        status.text = "● FLOTANTE DETENIDO"
        response.text = "El cerebro flotante fue detenido."
        Toast.makeText(this, "NEURO flotante detenido", Toast.LENGTH_SHORT).show()
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
