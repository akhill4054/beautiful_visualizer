package com.proto.beautifulvisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.SurfaceView

abstract class BaseVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    init {
        // To make the surface view transparent.
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    private var mLastAudioSessionId = -1

    @Volatile
    private var _isRendering = false

    @Volatile
    private lateinit var _values: IntArray

    // Max 1024
    private val mValuesCount = 0

    private var mVisualizer: Visualizer? = null

    private fun visualize(audioSessionId: Int) {
        mVisualizer?.release()

        mVisualizer = Visualizer(audioSessionId).apply {
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    p0: Visualizer?,
                    waveform: ByteArray?,
                    samplingRate: Int
                ) {
                    if (waveform != null) {
                        // I think byes in []byte waveform range from -128 to 128.
                        for (i in 0 until mValuesCount) {
                            _values[i] = waveform[i].toInt() - 128
                        }
                    }
                }

                override fun onFftDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)

            captureSize = 512
        }
    }

    private val renderer by lazy {
        Thread {
            while (_isRendering) {
                val canvas = holder.lockCanvas()
                // Clear canvas
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                // Update frame
                updateFrame(_values, canvas)
                // Post drawing
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    abstract fun updateFrame(values: IntArray, canvas: Canvas)

    private fun init(audioSessionId: Int) {
        mLastAudioSessionId = audioSessionId
        if (!::_values.isInitialized || _values.size != mLastAudioSessionId) {
            _values = IntArray(mValuesCount) { mValuesCount }
        }
    }

    fun render(audioSessionId: Int) {
        init(audioSessionId)

        _isRendering = true

        visualize(audioSessionId)
        mVisualizer!!.enabled = true

        renderer.start()
    }

    fun stop() {
        _isRendering = false

        mVisualizer?.enabled = false
        mVisualizer?.release()
        mVisualizer = null

        renderer.join()
    }

    fun restart() {
        stop()
        render(mLastAudioSessionId)
    }
}