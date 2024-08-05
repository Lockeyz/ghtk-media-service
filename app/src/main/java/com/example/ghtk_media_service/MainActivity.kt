package com.example.ghtk_media_service

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.ghtk_media_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val PLAY = 1
        const val PAUSE = 2
    }
    private var isPlaying = false

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false
    private var action = "PAUSE"
//    val context = MyApplication.instance
    var songName: String = ""
//    private var mediaPlayerListener: MediaPlayerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playPauseButton.setOnClickListener {
            setPlayPauseButton()
        }

        binding.increaseVolumeButton.setOnClickListener {
            setIncreaseVolumeButton()
        }

        binding.decreaseVolumeButton.setOnClickListener {
            setDecreaseVolumeButton()
        }

        binding.previousButton.setOnClickListener {
            stopService()
        }
    }

    private fun setPlayPauseButton() {
        senActionToForegroundService(action)
        if (action == "PLAY") {
            sendActionToService(action)
            binding.playPauseButton.setImageResource(R.drawable.play)
            action = "PAUSE"
        } else {
            sendActionToService(action)
            binding.playPauseButton.setImageResource(R.drawable.pause)
            action = "PLAY"
        }
    }

    private fun setIncreaseVolumeButton() {
            sendActionToService("INCREASE_VOLUME")
    }

    private fun setDecreaseVolumeButton() {
            sendActionToService("DECREASE_VOLUME")
    }

    fun resumeSong(playPause: ImageView) {
        if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
            updatePlayPauseButtonUI(playPause)
        }

    }

    fun pauseSong(playPause: ImageView) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
            updatePlayPauseButtonUI(playPause)
        }

    }

    private fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    private fun updatePlayPauseButtonUI(playPause: ImageView) {
        playPause.setImageResource(if (isPlaying()) R.drawable.pause else R.drawable.play)
    }

    private fun sendActionToService(action: String) {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.action = action
        startService(serviceIntent)
    }

    private fun senActionToForegroundService(action: String) {
        val foregroundServiceIntent = Intent(this, ForegroundService::class.java)
        foregroundServiceIntent.action = action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundServiceIntent)
        } else {
            startService(foregroundServiceIntent)
        }
    }

    private fun stopService() {
        val foregroundIntentService = Intent(this, ForegroundService::class.java)
        stopService(foregroundIntentService)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    override fun onStop() {
        super.onStop()
    }
}
