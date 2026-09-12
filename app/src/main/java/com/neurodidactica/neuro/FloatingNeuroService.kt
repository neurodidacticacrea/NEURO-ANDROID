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

    companion object {
        const val ACTION_STOP = "com.neurodidactica.neuro.STOP_FLOATING"
    }

    private lateinit var windowManager: WindowManager
    private var brain: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(77, buildNotification())

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val img = ImageView(this).apply {
            setImageResource(R.drawable.neuro_brain)
            alpha = 1f
            contentDescription = "NEURO flotante"
            setPadding(6, 6, 6, 6)
        }
        brain = img

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            220,
            220,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 300
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
                    runCatching { windowManager.updateViewLayout(img, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val now = System.currentTimeMillis()
                        val last = img.getTag(android.R.id.custom) as? Long ?: 0L
                        img.setTag(android.R.id.custom, now)

                        val launch = packageManager.getLaunchIntentForPackage(packageName)
                        if (now - last < 420L) {
                            launch?.putExtra("AUTO_LISTEN", true)
                        }
                        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (launch != null) startActivity(launch)
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(img, params)
        pulse(img)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun pulse(v: ImageView) {
        v.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .alpha(0.92f)
            .setDuration(900)
            .withEndAction {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(900)
                    .withEndAction { v.post { pulse(v) } }
                    .start()
            }.start()
    }

    override fun onDestroy() {
        brain?.let {
            if (::windowManager.isInitialized) {
                runCatching { windowManager.removeView(it) }
            }
        }
        brain = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "neuro_core",
                "NEURO activo",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Mantiene visible el cerebro flotante de NEURO."
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, FloatingNeuroService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "neuro_core")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("NEURO flotante activo")
            .setContentText("El cerebro está disponible sobre otras apps.")
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(0, "Detener", stopPending)
            .build()
    }
}
