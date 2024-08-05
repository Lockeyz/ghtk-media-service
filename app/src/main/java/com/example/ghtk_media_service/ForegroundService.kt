package com.example.ghtk_media_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager

    companion object {
        var instance: ForegroundService? = null
        private val TAG = ForegroundService::class.java.simpleName
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.e(TAG, "onCreate ${hashCode()}")
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            stopSelf()
        }
        mediaPlayer.setDataSource(
            applicationContext,
            Uri.parse("android.resource://" + packageName + "/" + R.raw.saika)
        )
        mediaPlayer.prepare()
        mediaPlayer.start()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelName = "Play Music Background Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                packageName,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, packageName)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tèn tén ten")
            .setContentText("Cùng nghe nhạc nào mọi người!")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getIntExtra("data", -1)
        val action = intent?.action
        Log.e(TAG, "onStartCommand Data: $data, Flag: $flags, StartId: $startId")
        when (action) {
            "PLAY" -> {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }
            "PAUSE" -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }
            "INCREASE_VOLUME" -> {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
            }
            "DECREASE_VOLUME" -> {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e(TAG, "onTaskRemoved")
        super.onTaskRemoved(rootIntent)

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        mediaPlayer.release()
        super.onDestroy()
    }

    fun pauseMedia() {
        mediaPlayer.pause()
    }
}