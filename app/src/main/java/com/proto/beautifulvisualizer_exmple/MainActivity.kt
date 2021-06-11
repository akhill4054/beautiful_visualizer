package com.proto.beautifulvisualizer_exmple

import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.proto.beautifulvisualizer_example.R
import com.proto.beautifulvisualizer_example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding

    private var _selectedMedia: Uri? = null

    private lateinit var _player: SimpleExoPlayer

    private val _seekBar get() = _binding.seekBar

    private val _seekBarHandler = Handler(Looper.getMainLooper())
    private val _seekBarRunnable = object : Runnable {
        override fun run() {
            if (_player.isPlaying) {
                val currentPosition = _player.currentPosition.toFloat() / _player.duration * 100
                _seekBar.value = currentPosition
            }
            _seekBarHandler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)

        _player = SimpleExoPlayer.Builder(this).build()

        setupViews()
    }

    private fun setupViews() {
        // Visualizer settings
        val barVisualizer = _binding.barVisualizer
        barVisualizer.settings.apply {
            barFillColor =
                ContextCompat.getColor(this@MainActivity, R.color.purple_500)
            isRainbowMode = true
            rainbowColors = intArrayOf(
                Color.parseColor("#302deb"),
                Color.parseColor("#732deb"),
                Color.parseColor("#8c2deb"),
                Color.parseColor("#cb2deb"),
                Color.parseColor("#60eb2d"),
                Color.parseColor("#eb2d5d"),
                Color.parseColor("#4e10c2"),
            )
            isDynamicRainbow = true
            isRoundedCorners = false
        }

        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    updateSelectedMedia(uri)
                }
            }

        val requestStoragePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Continue the flow
                getContent.launch("audio/*")
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        val requestRecordingPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Continue the flow
                _selectedMedia?.let {
                    playSong()
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Seekbar
        _binding.seekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                _player.seekTo((_player.duration * value / 100).toLong())
            }
        }

        // Start seekBar update
        _seekBarHandler.post(_seekBarRunnable)

        // Click listeners
        _binding.pickASong.setOnClickListener {
            if (_player.isPlaying) {
                stop()
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_DENIED
            ) {
                getContent.launch("audio/*")
            } else {
                requestStoragePermissionLauncher.launch(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
        _binding.playButton.setOnClickListener {
            _selectedMedia?.let {
                if (_player.isPlaying) {
                    _player.pause()
                    // Update view
                    _binding.playButton.setImageResource(
                        R.drawable.ic_baseline_play_circle_24
                    )
                } else if (_player.currentMediaItem != null) {
                    playSong()
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.RECORD_AUDIO,
                        ) != PackageManager.PERMISSION_DENIED
                    ) {
                        playSong()
                    } else {
                        // Request audio recording permission
                        requestRecordingPermissionLauncher.launch(
                            android.Manifest.permission.RECORD_AUDIO
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (_player.isPlaying) {
            _binding.barVisualizer.resume()
        }
    }

    override fun onStop() {
        super.onStop()

        _binding.barVisualizer.pause()
    }

    private fun updateSelectedMedia(uri: Uri) {
        // Reset media player
        _player.stop()

        // Update player media
        val mediaItem = MediaItem.fromUri(uri)
        _player.setMediaItem(mediaItem)

        _selectedMedia = uri

        enableMediaControls(true)

        // Update UI
        _binding.selectedMedia.text = uri.toString()
        _binding.playButton.setBackgroundResource(
            R.drawable.ic_baseline_play_circle_24
        )
    }

    private fun playSong() {
        // Start playing the song
        _player.prepare()
        _player.play()

        // Start the visualization
        _binding.barVisualizer.render(_player.audioSessionId)

        // Update view
        _binding.playButton.setImageResource(R.drawable.ic_baseline_pause_circle_24)
    }

    private fun enableMediaControls(isEnabled: Boolean) {
        _binding.run {
            if (isEnabled) {
                playButton.alpha = 1F
                playButton.isClickable = true

                seekBar.isEnabled = true
            } else {
                playButton.alpha = 0.5F
                playButton.isClickable = false
                playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_24)

                seekBar.value = 0F
                seekBar.isEnabled = false
            }
        }
    }

    private fun stop() {
        // UI
        enableMediaControls(false)
        _player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        _player.stop()
        _player.release()

        _binding.barVisualizer.stop()
    }
}