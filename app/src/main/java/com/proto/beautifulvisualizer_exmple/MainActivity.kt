package com.proto.beautifulvisualizer_exmple

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.proto.beautifulvisualizer_example.R
import com.proto.beautifulvisualizer_example.databinding.ActivityMainBinding

private const val KEY_LAST_MEDIA_URI = "last_media_uri"

class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding

    private var _selectedMedia: Uri? = null
    private var _mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)

        setupViews()
    }

    private fun setupViews() {
        // Visualizer settings
        val barVisualizer = _binding.barVisualizer
        barVisualizer.settings.apply {
            barFillColor = ContextCompat.getColor(this@MainActivity, R.color.purple_500)
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
                    playSong(it)
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Click listeners
        _binding.pickASong.setOnClickListener {
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
                if (_mediaPlayer?.isPlaying == true) {
                    pausePlayer()
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.RECORD_AUDIO,
                        ) != PackageManager.PERMISSION_DENIED
                    ) {
                        playSong(it)
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

    private fun updateSelectedMedia(uri: Uri) {
        this._selectedMedia = uri
        enablePlayButton(true)
        // Update view
        _binding.selectedMedia.text = uri.toString()
    }

    private fun playSong(mediaUri: Uri) {
        // Start playing the song
        _mediaPlayer = MediaPlayer.create(application, mediaUri)
        try {
            val mediaPlayer = _mediaPlayer!!

            // Plat the song
            mediaPlayer.start()

            // Seek!
            mediaPlayer.seekTo(1000 * 45)

            // Start the visualization
            _binding.barVisualizer.render(mediaPlayer.audioSessionId)

            // Update view
            _binding.playButton.setImageResource(R.drawable.ic_baseline_pause_circle_24)
        } catch (e: Exception) {
            e.printStackTrace()
            tryToStopMediaPlayer()
            // Reset view
            _binding.playButton.setImageResource(R.drawable.ic_baseline_play_circle_24)
            enablePlayButton(false)
            _binding.selectedMedia.text = getString(R.string.no_media_selected)
            // Display error
            Toast.makeText(this, "Error! Couldn't play the song.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun pausePlayer() {
        try {
            _mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Update view
        _binding.playButton.setImageResource(R.drawable.ic_baseline_play_circle_24)
    }

    private fun enablePlayButton(isEnabled: Boolean) {
        _binding.playButton.run {
            if (isEnabled) {
                alpha = 1F
                isClickable = true
            } else {
                alpha = 0.5F
                isClickable = false
            }
        }
    }

    private fun tryToStopMediaPlayer() {
        try {
            // Stop visualization
            _binding.barVisualizer.stop()

            _mediaPlayer?.stop()
            _mediaPlayer?.release()

            _mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        tryToStopMediaPlayer()
    }
}