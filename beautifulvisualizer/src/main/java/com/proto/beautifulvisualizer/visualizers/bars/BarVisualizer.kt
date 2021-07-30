package com.proto.beautifulvisualizer.visualizers.bars

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.proto.beautifulvisualizer.BaseVisualizer
import com.proto.beautifulvisualizer.R

class BarVisualizer @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseVisualizer(context, attrs, defStyleAttr) {

    var settings = BarVisualizerSettings()

    init {
        // Setting view attrs
        if (attrs != null) {
            val typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.BarVisualizer, 0, 0)

            settings.run {
                val barFillColor =
                    typedArray.getColor(
                        R.styleable.BarVisualizer_barFillColor,
                        settings.barFillColor
                    )
                this.barFillColor = barFillColor

                val numberOfBars =
                    typedArray.getInt(
                        R.styleable.BarVisualizer_numberOfBars,
                        settings.numberOfBars
                    )
                this.numberOfBars = numberOfBars
            }


            typedArray.recycle()
        }
    }

    private val _barFillPaint = Paint()
    private val _capFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Rainbow mode colors
    @ColorInt
    private var _rainbowFillColors: IntArray? = null
    private val rainbowFillColors get() = _rainbowFillColors!!

    // Velocity unit -> steps / frame
    private var _drvStepsPerFrame = 0F
    private var _tempDRStepCount = 0

    // Drawing properties
    private var _barSpanWidth = 0F
    private var _barMaxHeight = 0F

    private var _barWidth = 0F
    private var _barCorners = 0F
    private var _barStartOffset = 0F

    private fun reflectSettings() {
        val numberOfBars = settings.numberOfBars

        // Base visualizer settings
        super.mNumberOfExpectedValues = numberOfBars
        super.mIdealVelocity = settings.velocity
        super.mIsAcceleratedModeOn = settings.isAcceleratedModeOn
        super.mFPS = settings.fps

        // Dimensions
        _barSpanWidth = mCanvasWidth / numberOfBars
        _barWidth = _barSpanWidth * settings.barWidth
        _barStartOffset = _barSpanWidth - _barWidth

        val cornersOffset = if (settings.isRoundedCorners) {
            _barWidth
        } else {
            0F
        }

        _barMaxHeight =
            (mCanvasHeight * settings.maxBarHeight) - cornersOffset

        _barCorners = cornersOffset / 2

        // Colors
        _barFillPaint.color = settings.barFillColor
        _capFillPaint.color = settings.barFillColor

        setupRainbowMode()
    }

    private fun setupRainbowMode() {
        if (settings.isRainbowMode) {
            val fillColors = settings.rainbowColors

            if (fillColors == null || fillColors.isEmpty()) {
                throw RuntimeException("Rainbow colors can't be null or empty in rainbow mode!")
            } else if (fillColors.size < 2) {
                throw RuntimeException("Please provide at least 2 colors for the rainbow mode!")
            }

            _rainbowFillColors = IntArray(mNumberOfExpectedValues) { 0 }

            // Generate fill colors
            val groupSize = rainbowFillColors.size / (fillColors.size - 1)

            for (i in 0 until (fillColors.size - 1)) {
                var color1 = fillColors[i]
                val color2 = fillColors[i + 1]

                val startIndex = i * groupSize
                val endIndex = if (i == fillColors.size - 2) {
                    rainbowFillColors.size - 1
                } else {
                    i * groupSize + groupSize - 1
                }

                rainbowFillColors[startIndex] = color1
                rainbowFillColors[endIndex] = color2

                for (j in (startIndex + 1) until endIndex) {
                    val newColor = ColorUtils.blendARGB(color1, color2, 0.5F)
                    rainbowFillColors[j] = newColor
                    color1 = newColor
                }
            }

            if (settings.isDynamicRainbow) {
                val dynamicRainbowVelocity = settings.dynamicRainbowVelocity
                _drvStepsPerFrame =
                    mFPS / (mNumberOfExpectedValues * dynamicRainbowVelocity)
            }
        } else {
            // Just in case
            settings.isDynamicRainbow = false
        }
    }

    override fun render(audioSessionId: Int) {
        // Init fields before rendering
        reflectSettings()

        super.render(audioSessionId)
    }

    override fun updateFrame(values: FloatArray, canvas: Canvas) {
        // Clear bitmap
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        if (settings.isDynamicRainbow) {
            _tempDRStepCount += 1
            if (_tempDRStepCount >= _drvStepsPerFrame) {
                // Rotate colors by 1 step.
                var prevIndexValue = rainbowFillColors[0]

                for (j in 1 until rainbowFillColors.size) {
                    val currIndexValue = rainbowFillColors[j]
                    rainbowFillColors[j] = prevIndexValue
                    prevIndexValue = currIndexValue
                }

                rainbowFillColors[0] = prevIndexValue

                _tempDRStepCount = 0
            }
        }

        for (i in values.indices) {
            if (settings.isRainbowMode) {
                _barFillPaint.color = rainbowFillColors[i]
                _capFillPaint.color = rainbowFillColors[i]
            }

            val barHeight = _barMaxHeight * values[i]

            val barLeft = i * _barSpanWidth + _barStartOffset
            val barBottom = mCanvasHeight - _barCorners
            val barTop = barBottom - barHeight
            val barRight = barLeft + _barWidth

            // Drawing only the upper part
            canvas.drawRect(
                barLeft,
                barTop,
                barRight,
                barBottom,
                _barFillPaint,
            )
            if (barHeight > 0 && settings.isRoundedCorners) {
                // Top cap
                canvas.drawArc(
                    barLeft,
                    barTop - _barCorners,
                    barRight,
                    barTop + _barCorners,
                    -180F,
                    180F,
                    true,
                    _barFillPaint,
                )
                canvas.drawArc(
                    barLeft,
                    barBottom - _barCorners,
                    barRight,
                    mCanvasHeight,
                    180F,
                    360F,
                    true,
                    _barFillPaint,
                )
            }
        }
    }
}