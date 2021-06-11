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
    var numberOfBars: Int = 40,
    var barWidth: Float = 0.8F,
    var maxBarHeight: Float = 0.8F,
    var isRoundedCorners: Boolean = true,
    var isAcceleratedModeOn: Boolean = true,
    @ColorInt
    var barFillColor: Int = Color.CYAN,
    var isRainbowMode: Boolean = false,
    @ColorInt
    var rainbowColors: IntArray? = null,
    var isDynamicRainbow: Boolean = false,
    var dynamicRainbowVelocity: Float = 0.2F,
    override var velocity: Float = 1.1F,
    override var fps: Int = 30,
) : VisualizerSettings()