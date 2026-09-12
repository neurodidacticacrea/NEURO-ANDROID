package com.neurodidactica.neuro

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class NeuroCommandRouter(
    private val context: Context,
    private val respond: (String) -> Unit
) {

    fun handle(raw: String) {
        val t = raw.trim()
        val n = t.lowercase(Locale("es", "MX"))

        when {
            n.contains("abre spotify") || n.contains("pon música") || n.contains("pon musica") -> {
                openPackage("com.spotify.music", "https://open.spotify.com/")
                respond("Abriendo Spotify.")
            }

            n.contains("abre whatsapp") -> {
                openPackage("com.whatsapp", "https://wa.me/")
                respond("Abriendo WhatsApp.")
            }

            n.contains("abre youtube") -> {
                openPackage("com.google.android.youtube", "https://youtube.com/")
                respond("Abriendo YouTube.")
            }

            n.contains("abre maps") || n.contains("abre mapas") -> {
                openPackage("com.google.android.apps.maps", "https://maps.google.com/")
                respond("Abriendo Mapas.")
            }

            n.startsWith("llama al ") || n.startsWith("llamar al ") -> {
                val number = t.filter { it.isDigit() || it == '+' }
                if (number.isNotBlank()) {
                    val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(i)
                    respond("Preparando la llamada a $number.")
                } else respond("No pude identificar el número.")
            }

            n.contains("alarma") -> {
                val regex = Regex("""(\d{1,2})(?::(\d{2}))?""")
                val m = regex.find(n)
                if (m != null) {
                    val hour = m.groupValues[1].toInt().coerceIn(0,23)
                    val min = m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toInt() ?: 0
                    val i = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, min)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "Alarma de NEURO")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(i)
                    respond("Preparando alarma a las %02d:%02d.".format(hour,min))
                } else {
                    val i = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(i)
                    respond("Abriendo tus alarmas.")
                }
            }

            n.contains("mis fotos") || n.contains("galería") || n.contains("galeria") -> {
                val i = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(i, "Fotos").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                respond("Abriendo tus fotos.")
            }

            n.contains("crea un plan") || n.contains("haz un plan") || n.contains("planea") -> {
                NeuroAiClient.ask(t) { answer ->
                    respond(answer ?: "Puedo crear el plan cuando conectemos el cerebro de IA de NEURO.")
                }
            }

            n.contains("agenda") || n.contains("calendario") -> {
                val i = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(i) }.onFailure {
                    respond("No encontré una app de calendario predeterminada.")
                }
                respond("Abriendo tu agenda.")
            }

            else -> {
                NeuroAiClient.ask(t) { answer ->
                    respond(answer ?: "Entendí: “$t”. La conversación completa se activará al conectar el backend de IA.")
                }
            }
        }
    }

    private fun openPackage(pkg: String, fallback: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
