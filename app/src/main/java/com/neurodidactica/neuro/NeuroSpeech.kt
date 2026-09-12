package com.neurodidactica.neuro

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object NeuroSpeech {
    private var tts: TextToSpeech? = null

    fun say(context: Context, text: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("es", "MX")
                    tts?.setSpeechRate(1.02f)
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NEURO")
                }
            }
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NEURO")
        }
    }
}
