package com.neurodidactica.neuro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && Settings.canDrawOverlays(context)) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FloatingNeuroService::class.java)
            )
        }
    }
}
