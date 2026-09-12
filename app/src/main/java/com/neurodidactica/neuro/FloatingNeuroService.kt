package com.neurodidactica.neuro

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class FloatingNeuroService : Service() {
    private lateinit var windowManager: WindowManager
    private var brain: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(77, buildNotification())

        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val img = ImageView(this).apply {
            setImageResource(R.drawable.neuro_brain)
            alpha = 0.96f
            contentDescription = "NEURO flotante"
        }
        brain = img

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            190, 190, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 250
        }

        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var moved = false

        img.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startX = params.x
                    startY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - downX).toInt()
                    val dy = (e.rawY - downY).toInt()
                    moved = abs(dx) + abs(dy) > 12
                    params.x = startX + dx
                    params.y = startY + dy
                    windowManager.updateViewLayout(img, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val launch = packageManager.getLaunchIntentForPackage(packageName)
                        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launch)
                    }
                    true
                }
                else -> false
            }
        }

        img.animate().scaleX(1.06f).scaleY(1.06f).setDuration(850).withEndAction {
            pulse(img)
        }.start()

        windowManager.addView(img, params)
    }

    private fun pulse(v: ImageView) {
        v.animate().scaleX(0.98f).scaleY(0.98f).alpha(0.9f).setDuration(900).withEndAction {
            v.animate().scaleX(1.06f).scaleY(1.06f).alpha(1f).setDuration(900).withEndAction {
                v.post { pulse(v) }
            }.start()
        }.start()
    }

    override fun onDestroy() {
        brain?.let {
            if (::windowManager.isInitialized) runCatching { windowManager.removeView(it) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(
                "neuro_core",
                "NEURO activo",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
    }

    private fun buildNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, "neuro_core")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("NEURO está activo")
            .setContentText("Tu inteligencia personal está disponible.")
            .setOngoing(true)
            .setContentIntent(pending)
            .build()
    }
}
