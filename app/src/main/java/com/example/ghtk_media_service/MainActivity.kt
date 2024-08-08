package com.example.ghtk_media_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ghtk_media_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val PLAY = 1
        const val PAUSE = 2
        const val INCREASE_VOLUME = 3
        const val DECREASE_VOLUME = 4
        const val GET_SONG_NAME = 5
        const val GET_STATE = 6
    }
    private var action = PLAY

    private lateinit var songNameReceiver: BroadcastReceiver
    private lateinit var songProgressReceiver: BroadcastReceiver
    private lateinit var songStateReceiver: BroadcastReceiver

    private var currentPosition = 0
    private var duration = 1
    private var progress = 0
    private var isPlaying = false
    private var name = ""
    private var volume = 0
    private var maxVolume = 1
    private var progressVolume = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đăng ký BroadcastReceiver để cập nhật ProgressBar
        songProgressReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentPosition = intent?.getIntExtra("current_position", 0) ?: 0
                duration = intent?.getIntExtra("duration", 1) ?: 1
                isPlaying = intent?.getBooleanExtra("state", false) ?: false
                name = intent?.getStringExtra("name") ?: ""
                volume = intent?.getIntExtra("volume", 0) ?: 0
                maxVolume = intent?.getIntExtra("max_volume", 1) ?: 1
                // Cập nhật ProgressBar
                progress = (currentPosition * 100) / duration
                binding.linearProgressBar.progress = progress
                // Cập nhật TextView hiển thị thời gian hiện tại và tổng thời gian
                binding.tvCurrentTime.text = currentPosition.toString()
                binding.tvDuration.text = duration.toString()
                // Cập nhật tên bài hát
                binding.tvTitleSong.text = name
                // Cập nhật volume
                progressVolume = (volume * 100) / maxVolume
                binding.volumeBar.progress = progressVolume

                if (!isPlaying) {
                    binding.playPauseButton.setImageResource(R.drawable.play)
                }
            }
        }

        val progressFilter = IntentFilter("com.example.ghtk_media_service.SONG_PROGRESS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(songProgressReceiver, progressFilter, RECEIVER_EXPORTED)
        }

        senActionToForegroundService(GET_STATE)

        binding.playPauseButton.setOnClickListener {
            setPlayPauseButton()
            Log.e("MA", (!isPlaying).toString())
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
        Log.e("MA", (isPlaying).toString())
        if (!isPlaying) {
            senActionToForegroundService(PLAY)
            binding.playPauseButton.setImageResource(R.drawable.pause)
//            isPlaying = true
        } else {
            senActionToForegroundService(PAUSE)
            binding.playPauseButton.setImageResource(R.drawable.play)
//            isPlaying = false
        }
    }

    private fun setIncreaseVolumeButton() {
        senActionToForegroundService(INCREASE_VOLUME)
    }

    private fun setDecreaseVolumeButton() {
        senActionToForegroundService(DECREASE_VOLUME)
    }

    private fun sendActionToService(action: String) {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.action = action
        startService(serviceIntent)
    }

    private fun senActionToForegroundService(action: Int) {
        val foregroundServiceIntent = Intent(this, ForegroundService::class.java)
        foregroundServiceIntent.putExtra("action", action)
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

}
