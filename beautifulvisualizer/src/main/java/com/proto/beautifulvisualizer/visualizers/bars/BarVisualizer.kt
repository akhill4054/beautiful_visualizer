package com.proto.beautifulvisualizer.visualizers.bars

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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

        // Dimensions
        _barSpanWidth = mCanvasWidth / numberOfBars
        _barWidth = _barSpanWidth * settings.barWidth
        _barStartOffset = _barSpanWidth - _barWidth

        _barMaxHeight =
            (mCanvasHeight * settings.maxBarHeight / 2) - (2 * _barWidth)

        _barCorners = _barWidth / 2

        // Colors
        _barFillPaint.color = settings.barFillColor
        _capFillPaint.color = settings.barFillColor
    }

    override fun render(audioSessionId: Int) {
        // Init fields before rendering
        reflectSettings()

        super.render(audioSessionId)
    }

    override fun updateFrame(values: FloatArray, canvas: Canvas) {
        // Clear bitmap
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        for (i in values.indices) {
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
            if (barHeight > 0) {
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