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
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.slider.Slider
import com.proto.beautifulvisualizer_example.R
import com.proto.beautifulvisualizer_example.databinding.ActivityMainBinding

// Player state
sealed class PlayerState
object Ideal : PlayerState()
object Playing : PlayerState()
object Paused : PlayerState()

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

        _binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        _player = SimpleExoPlayer.Builder(this).build()

        setupViews()
    }

    // Beginning of the experiment
    private fun setupViews() {
        // Init data-binding fields
        _binding.noMediaSelected = true
        _binding.playerState = Ideal

        // Visualizer settings
        val barVisualizer = _binding.barVisualizer
        barVisualizer.settings.apply {
            barFillColor =
                ContextCompat.getColor(this@MainActivity, R.color.purple_500)
            isRainbowMode = _binding.rainbowMode.isChecked
            rainbowColors = intArrayOf(
                Color.parseColor("#302deb"),
                Color.parseColor("#732deb"),
                Color.parseColor("#8c2deb"),
                Color.parseColor("#cb2deb"),
                Color.parseColor("#60eb2d"),
                Color.parseColor("#eb2d5d"),
                Color.parseColor("#4e10c2"),
                Color.parseColor("#302de1"),
                Color.parseColor("#732de2"),
                Color.parseColor("#8c2de3"),
                Color.parseColor("#cb2de4"),
                Color.parseColor("#60eb25"),
                Color.parseColor("#eb2d56"),
                Color.parseColor("#4e10c7"),
            )
            isDynamicRainbow = _binding.dynamicRainbow.isChecked
            isRoundedCorners = _binding.roundedCorners.isChecked

            numberOfBars = _binding.numberOfBars.value.toInt()
            barWidth = _binding.barWidth.value
            velocity = _binding.barVelocity.value
            fps = _binding.fps.value.toInt()
            dynamicRainbowVelocity = _binding.dynamicRainbowVelocity.value
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

        val reflectSettings = { barVisualizer.restart() }
        val getVisualizerSettings = { barVisualizer.settings }

        // Visualizer setting controls
        _binding.roundedCorners.setOnCheckedChangeListener { _, isChecked ->
            getVisualizerSettings().isRoundedCorners = isChecked
            reflectSettings()
        }
        _binding.rainbowMode.setOnCheckedChangeListener { _, isChecked ->
            getVisualizerSettings().isRainbowMode = isChecked
            reflectSettings()
        }
        _binding.dynamicRainbow.setOnCheckedChangeListener { _, isChecked ->
            getVisualizerSettings().isDynamicRainbow = isChecked
            reflectSettings()
        }
        _binding.numberOfBars.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getVisualizerSettings().numberOfBars = slider.value.toInt()
                reflectSettings()
            }
        })
        _binding.fps.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getVisualizerSettings().fps = slider.value.toInt()
                reflectSettings()
            }
        })
        _binding.barWidth.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getVisualizerSettings().barWidth = slider.value
                reflectSettings()
            }
        })

        _binding.barVelocity.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getVisualizerSettings().velocity = slider.value
                reflectSettings()
            }
        })
        _binding.dynamicRainbowVelocity.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                getVisualizerSettings().dynamicRainbowVelocity = slider.value
                reflectSettings()
            }
        })

        // Click listeners
        _binding.pickASong.setOnClickListener {
            if (_player.isPlaying) {
                stopPlayer()
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
                    _binding.playerState = Paused
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
        // Stop the player
        stopPlayer()

        // Update player media
        val mediaItem = MediaItem.fromUri(uri)
        _player.setMediaItem(mediaItem)

        _selectedMedia = uri

        // Update UI
        _binding.noMediaSelected = false
        _binding.selectedMedia.text = uri.toString()
    }

    private fun playSong() {
        // Start playing the song
        _player.prepare()
        _player.play()

        // Start the visualization
        _binding.barVisualizer.render(_player.audioSessionId)

        // Update view
        _binding.playerState = Playing
    }

    private fun stopPlayer() {
        _binding.playerState = Ideal
        _player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        _player.stop()
        _player.release()

        _binding.barVisualizer.stop()
    }
}