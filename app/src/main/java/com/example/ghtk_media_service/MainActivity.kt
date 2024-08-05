package com.example.ghtk_media_service

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playPauseButton = findViewById<Button>(R.id.playPauseButton)
        val increaseVolumeButton = findViewById<Button>(R.id.increaseVolumeButton)
        val decreaseVolumeButton = findViewById<Button>(R.id.decreaseVolumeButton)

        playPauseButton.setOnClickListener {
            val action = if (isPlaying) "PAUSE" else "PLAY_PAUSE"
            sendActionToService(action)
            isPlaying = !isPlaying
            playPauseButton.text = if (isPlaying) "Pause" else "Play"
        }

        increaseVolumeButton.setOnClickListener {
            sendActionToService("INCREASE_VOLUME")
        }

        decreaseVolumeButton.setOnClickListener {
            sendActionToService("DECREASE_VOLUME")
        }
    }

    private fun sendActionToService(action: String) {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.action = action
        startService(serviceIntent)
    }
}
