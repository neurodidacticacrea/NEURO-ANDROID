package com.neurodidactica.neuro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpdateCenter {
    // Change this later to your GitHub Releases page or secure update endpoint.
    const val RELEASES_URL = "https://github.com/NEURODIDACTICA/NEURO-ANDROID/releases"

    fun open(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {
            Toast.makeText(context, "Centro de actualizaciones aún no configurado.", Toast.LENGTH_LONG).show()
        }
    }
}
