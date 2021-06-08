package com.proto.beautifulvisualizer

/**
 * @see velocity, value ranging from 0-infinity.
 * It's value is measured as % of height of visualizer view
 * per second, where value 0 is 0% and 1F is 100%, 1.5 is 150% .. and so on.
 * e.g.
 * @see velocity = 0.6F means velocity will be 60% height
 * of visualizer view / second.
 * */
abstract class VisualizerSettings {
    abstract var velocity: Float
}