package com.proto.beautifulvisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

private const val TAG = "BaseVisualizer"

abstract class BaseVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    init {
        // To make the surface view transparent.
        this.setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    private var mLastAudioSessionId = -1

    private val mIsRendering = AtomicBoolean(false)

    val isRendering: Boolean get() = mIsRendering.get()

    @Volatile
    private lateinit var _values: IntArray

    // Base visualizer settings
    // Max 1024
    protected var mNumberOfExpectedValues = 0
    protected var mIsAcceleratedModeOn = false

    private var mVisualizer: Visualizer? = null

    // Drawing attrs
    protected var mCanvasWidth = 0F
    protected var mCanvasHeight = 0F

    protected var mIdealVelocity = 0F

    protected var mFPS = 60

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
                        val incrementValue = waveform.size / _values.size
                        // I think byes in []byte waveform always range from -128 to 128.
                        var i = 0
                        var j = 0
                        synchronized(_values) {
                            while (j < _values.size) {
                                _values[j] = waveform[i].toInt()
                                i += incrementValue
                                j++
                            }
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
        Renderer()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCanvasHeight = h.toFloat()
        mCanvasWidth = w.toFloat()

        if (isRendering) {
            restart()
        }
    }

    /**
     * @param values, an array of float values representing waveform
     * of the music ranging from 0-1.
     * Size of values is determined by numberOfExpectedValues.
     * */
    protected abstract fun updateFrame(values: FloatArray, canvas: Canvas)

    private fun init(audioSessionId: Int) {
        mLastAudioSessionId = audioSessionId

        // Init fields before rendering
        if (!::_values.isInitialized || _values.size != mLastAudioSessionId) {
            _values = IntArray(mNumberOfExpectedValues) { mNumberOfExpectedValues }
        }

        // Update velocity for values
        mIdealVelocity /= mFPS
    }

    open fun render(audioSessionId: Int) {
        init(audioSessionId)

        visualize(audioSessionId)
        mVisualizer!!.enabled = true

        renderer.start()
    }

    open fun stop() {
        mVisualizer?.enabled = false
        mVisualizer?.release()
        mVisualizer = null

        renderer.stop()
    }

    open fun restart() {
        stop()
        render(mLastAudioSessionId)
    }

    private inner class Renderer {
        private lateinit var lastProvidedValues: FloatArray
        private lateinit var currentValues: FloatArray

        private var _rendererThread: Thread? = null

        fun start() {
            if (_rendererThread != null || isRendering) {
                stop()
            }

            mIsRendering.set(true)

            _rendererThread = Thread {

                val idealFrameRenderingTime = 1000L / mFPS
                var lastFrameTimestamp = System.currentTimeMillis()

                lastProvidedValues =
                    FloatArray(mNumberOfExpectedValues) { 0F }
                currentValues =
                    lastProvidedValues.copyOf()

                while (true) {
                    if (!isRendering) break

                    val canvas = holder.lockCanvas()

                    if (canvas == null) {
                        try {
                            // Free the lock
                            holder.unlockCanvasAndPost(canvas)
                            stop()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        break
                    }

                    // Clear canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    // To avoid blocking of main thread for long time
                    synchronized(_values) {
                        for (i in _values.indices) {
                            lastProvidedValues[i] = _values[i].toFloat()
                        }
                    }
                    Log.d(TAG, "start: running...")

                    for (i in lastProvidedValues.indices) {
                        // Justify local provided values
                        lastProvidedValues[i] = (128F + lastProvidedValues[i]) / 256

                        if (lastProvidedValues[i] >= 0.9F) {
                            // Randomizing the value
                            lastProvidedValues[i] = 0.7F + Random.nextFloat() * 0.3F
                        }

                        // Update current values
                        val diff = lastProvidedValues[i] - currentValues[i]

                        if (diff > 0) {
                            currentValues[i] += mIdealVelocity

                            if (currentValues[i] > lastProvidedValues[i]) {
                                currentValues[i] = lastProvidedValues[i]
                            }
                        } else {
                            currentValues[i] -= mIdealVelocity

                            if (currentValues[i] < lastProvidedValues[i]) {
                                currentValues[i] = lastProvidedValues[i]
                            }
                        }
                    }

                    // Update frame
                    updateFrame(currentValues, canvas)

                    // Post drawing
                    holder.unlockCanvasAndPost(canvas)

                    val currTime = System.currentTimeMillis()
                    val frameRenderingTime = currTime - lastFrameTimestamp

                    if (isRendering) {
                        val sleepTime = idealFrameRenderingTime - frameRenderingTime
                        if (sleepTime > 0) {
                            sleep(sleepTime)
                        }
                        Log.d(
                            TAG,
                            "start: fps = ${1000 / frameRenderingTime}, sleep = $sleepTime"
                        )
                    }

                    lastFrameTimestamp = currTime
                }
            }

            _rendererThread!!.start()
        }

        fun stop() {
            mIsRendering.set(false)
            _rendererThread?.join()
            _rendererThread = null
        }
    }
}