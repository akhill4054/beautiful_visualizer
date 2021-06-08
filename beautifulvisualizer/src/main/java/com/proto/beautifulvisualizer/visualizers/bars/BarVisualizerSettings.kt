package com.proto.beautifulvisualizer.visualizers.bars

import android.graphics.Color
import androidx.annotation.ColorInt
import com.proto.beautifulvisualizer.VisualizerSettings

/**
 * @param barWidth, value ranging from 0-1.
 * @param maxBarHeight, value ranging from 0-1.
 * @param velocity,
 * @see VisualizerSettings.velocity
 * */
class BarVisualizerSettings(
    var numberOfBars: Int = 33,
    var barWidth: Float = 0.8F,
    var maxBarHeight: Float = 1.1F,
    var isAcceleratedModeOn: Boolean = true,
    @ColorInt
    var barFillColor: Int = Color.CYAN,
    override var velocity: Float = 1.5F
) : VisualizerSettings()