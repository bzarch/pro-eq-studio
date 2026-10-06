package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class DspBackgroundService : Service {

    constructor() : super()

    companion object {
        const val CHANNEL_ID = "pro_dsp_audio_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_SYSTEM_DSP = "com.example.action.START_SYSTEM_DSP"
        const val ACTION_STOP_SYSTEM_DSP = "com.example.action.STOP_SYSTEM_DSP"
        const val ACTION_TOGGLE_BYPASS = "com.example.action.TOGGLE_BYPASS"

        fun startService(context: Context) {
            val intent = Intent(context, DspBackgroundService::class.java).apply {
                action = ACTION_START_SYSTEM_DSP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DspBackgroundService::class.java).apply {
                action = ACTION_STOP_SYSTEM_DSP
            }
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SYSTEM_DSP) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildPersistentNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PRO DSP System Processing Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps DSP processing active in background for Spotify, YouTube, etc."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PRO DSP AUDIO STUDIO")
            .setContentText("SYSTEM DSP ACTIVE • Processing Audio")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
