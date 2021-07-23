package org.fairy.next.util

const val PI          = 3.1415926536f
const val HALF_PI     = PI * 0.5f
const val TWO_PI      = PI * 2.0f
const val FOUR_PI     = PI * 4.0f
const val INV_PI      = 1.0f / PI
const val INV_TWO_PI  = INV_PI * 0.5f
const val INV_FOUR_PI = INV_PI * 0.25f

inline fun clamp(x: Float, min: Float, max: Float)= if (x < min) min else (if (x > max) max else x)

inline fun saturate(x: Float) = clamp(x, 0.0f, 1.0f)

inline fun mix(a: Float, b: Float, x: Float) = a * (1.0f - x) + b * x

inline fun degrees(v: Float) = v * (180.0f * INV_PI)

inline fun radians(v: Float) = v * (PI / 180.0f)

inline fun fract(v: Float) = v % 1

inline fun sqr(v: Float) = v * v

inline fun pow(x: Float, y: Float) = StrictMath.pow(x.toDouble(), y.toDouble()).toFloat()