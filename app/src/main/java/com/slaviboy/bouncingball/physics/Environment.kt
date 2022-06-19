package com.slaviboy.bouncingball.physics

/**
 * @param gravity (1G) gravity, value between [0,10]
 * @param fluidDensity fluid density, value between [0,1000]
 * @param dragCoefficient drag coefficient, value between [0,1]
 * @param gravitationalAcceleration here on earth
 * @param refreshRate refresh rate 1/fps
 */
data class Environment(
    var gravity: Float = 1.0f,
    var fluidDensity: Float = 1.22f,
    var dragCoefficient: Float = 0.47f,
    var gravitationalAcceleration: Float = 9.81f,
    var refreshRate: Float = (1.0f / 60.0f)
)