package com.neurodidactica.neuro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.os.CountDownTimer
import java.util.Locale

class NeuroCommandRouter(
    private val context: Context,
    private val respond: (String) -> Unit
) {

    fun handle(raw: String) {
        val t = raw.trim()
        val n = t.lowercase(Locale("es", "MX"))
        LocalHistory.add(context, t)

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
                    // SECURITY: Opens the dialer; NEURO does not place the call silently.
                    val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(i)
                    respond("Preparé el número. Tú confirmas la llamada desde el marcador.")
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
                    context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    respond("Abriendo tus alarmas.")
                }
            }

            n.contains("borra") || n.contains("elimina") -> {
                // SECURITY: destructive commands are never performed silently in this beta.
                respond("Por seguridad no borraré nada de forma silenciosa. Esa acción requerirá una pantalla de confirmación de Android.")
            }

            n.contains("paga") || n.contains("transfiere") || n.contains("banco") ||
            n.contains("tarjeta") || n.contains("compra") -> {
                respond("Modo financiero protegido: NEURO no ejecuta pagos, transferencias ni operaciones bancarias.")
            }

            n.contains("mis fotos") || n.contains("galería") || n.contains("galeria") -> {
                // SECURITY: no broad media permission; just open the user's chosen gallery app.
                val i = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(i, "Fotos").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                respond("Abriendo la galería. NEURO no tiene acceso masivo a tus fotos.")
            }

            n.contains("agenda") || n.contains("calendario") -> {
                val i = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(i) }
                    .onSuccess { respond("Abriendo tu agenda.") }
                    .onFailure { respond("No encontré una app de calendario predeterminada.") }
            }


            n.contains("temporizador") || n.contains("timer") -> {
                val regex = Regex("""(\d+)\s*(minuto|minutos|min|segundo|segundos|seg)""")
                val m = regex.find(n)
                if (m != null) {
                    val value = m.groupValues[1].toLong()
                    val unit = m.groupValues[2]
                    val millis = if (unit.startsWith("min")) value * 60_000L else value * 1_000L
                    respond("Temporizador iniciado por $value ${if (unit.startsWith("min")) "minutos" else "segundos"}.")
                    object : CountDownTimer(millis, 1000L) {
                        override fun onTick(ms: Long) {}
                        override fun onFinish() {
                            respond("Temporizador terminado.")
                        }
                    }.start()
                } else {
                    respond("Dime la duración, por ejemplo: temporizador de 5 minutos.")
                }
            }

            n.contains("buenos días") || n.contains("buenos dias") -> {
                respond("Buenos días. NEURO está activo. Puedo abrir tu agenda, música o mapas cuando me lo pidas.")
            }

            n.contains("voy a trabajar") || n.contains("modo trabajo") -> {
                respond("Modo trabajo listo. Abriendo tu agenda.")
                val i = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(i) }
            }

            n.contains("conducir") || n.contains("modo conducción") || n.contains("modo conduccion") -> {
                respond("Modo conducción. Abriendo Mapas.")
                openPackage("com.google.android.apps.maps", "https://maps.google.com/")
            }

            n.contains("voy a dormir") || n.contains("buenas noches") -> {
                respond("Modo descanso activado. Buenas noches.")
            }

            n.contains("historial") -> {
                respond(LocalHistory.text(context))
            }

            n.contains("actualización") || n.contains("actualizacion") -> {
                respond("Abriendo el centro de actualizaciones.")
                UpdateCenter.open(context)
            }

            else -> {
                if (SecurityPreferences.isPrivateMode(context)) {
                    respond("Modo privado activo. Esta consulta no saldrá del teléfono. Desactiva el modo privado sólo cuando quieras usar la IA en línea.")
                } else {
                    NeuroAiClient.ask(t) { answer ->
                        respond(answer ?: "No pude conectar con el agente de IA.")
                    }
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
