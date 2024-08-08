package com.example.ghtk_media_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ghtk_media_service.MainActivity.Companion.DECREASE_VOLUME
import com.example.ghtk_media_service.MainActivity.Companion.GET_SONG_NAME
import com.example.ghtk_media_service.MainActivity.Companion.GET_STATE
import com.example.ghtk_media_service.MainActivity.Companion.INCREASE_VOLUME
import com.example.ghtk_media_service.MainActivity.Companion.PAUSE
import com.example.ghtk_media_service.MainActivity.Companion.PLAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager

    companion object {
        var instance: ForegroundService? = null
        private val TAG = ForegroundService::class.java.simpleName
    }

    private var progressUpdateJob: Job? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.e(TAG, "onCreate ${hashCode()}")
        mediaPlayer = MediaPlayer()
        mediaPlayer.isLooping = false
        mediaPlayer.setOnCompletionListener {
            stopSelf()
//            mediaPlayer.stop()
//            mediaPlayer.prepare()
//            sendStateBroadcast()
        }
        mediaPlayer.setDataSource(
            applicationContext,
            Uri.parse("android.resource://" + packageName + "/" + R.raw.as_it_was)
        )
        mediaPlayer.prepare()

        // Initialize AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        sendProgressBroadcast()

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
            .setContentTitle("Đang phát nhạc")
            .setContentText("Cùng nghe nhạc nào mọi người!")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getIntExtra("data", -1)
        val action = intent?.getIntExtra("action", -1)
        Log.e(TAG, "onStartCommand Data: $data, Flag: $flags, StartId: $startId")
        when (action) {
            PLAY -> {
                mediaPlayer.start()
                startProgressUpdates()
            }
            PAUSE -> {
                mediaPlayer.pause()
                stopProgressUpdates()
                sendProgressBroadcast()

            }
            INCREASE_VOLUME -> {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                sendProgressBroadcast()

            }
            DECREASE_VOLUME -> {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                sendProgressBroadcast()
            }
            GET_SONG_NAME -> {

            }
            GET_STATE -> {
                sendProgressBroadcast()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e(TAG, "onTaskRemoved")
        super.onTaskRemoved(rootIntent)

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        Log.e(TAG, "onDestroy")
    }

    private fun startProgressUpdates() {
        progressUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            while (mediaPlayer.isPlaying) {
                sendProgressBroadcast()
                delay(1000) // Cập nhật mỗi giây
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
    }

    private fun sendProgressBroadcast() {
        val intent = Intent("com.example.ghtk_media_service.SONG_PROGRESS")
        intent.putExtra("current_position", mediaPlayer.currentPosition)
        intent.putExtra("duration", mediaPlayer.duration)
        intent.putExtra("state", mediaPlayer.isPlaying)
        intent.putExtra("name", R.raw.as_it_was.toString())
        intent.putExtra("volume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        intent.putExtra("max_volume", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        sendBroadcast(intent)
    }
}