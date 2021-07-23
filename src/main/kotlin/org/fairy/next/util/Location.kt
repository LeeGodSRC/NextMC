package org.fairy.next.util

import org.fairy.next.extension.mc
import org.fairy.next.world.World
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

data class Location(
    private var worldName: String,
    private var _x: Double,
    private var _y: Double,
    private var _z: Double,
    private var _yaw: Float = 0.0f,
    private var _pitch: Float = 0.0f
) {

    constructor(worldName: String, x: Int, y: Int, z: Int, yaw: Float = 0.0f, pitch: Float = 0.0f): this(
        worldName,
        x.toDouble(),
        y.toDouble(),
        z.toDouble(),
        yaw,
        pitch
    )

    var x: Double
        get() = _x
        set(value) {
            _x = if (value.isNaN()) {
                0.0
            } else {
                value
            }
        }

    var y: Double
        get() = _y
        set(value) {
            _y = if (value.isNaN()) {
                0.0
            } else {
                value
            }
        }

    var z: Double
        get() = _z
        set(value) {
            _z = if (value.isNaN()) {
                0.0
            } else {
                value
            }
        }

    var yaw: Float
        get() = _yaw
        set(value) {
            _yaw = if (value.isNaN()) {
                0.0F
            } else {
                value
            }
        }

    var pitch: Float
        get() = _pitch
        set(value) {
            _pitch = if (value.isNaN()) {
                0.0F
            } else {
                value
            }
        }

    val chunkX: Int
        get() = x.floor() shr 4

    val chunkZ: Int
        get() = z.floor() shr 4

    var world: World?
        get() = mc.worldContainer.get(this.worldName)
        set(value) {
            worldName = value?.name!!
        }

    val blockX: Int
        get() = locToBlock(x)
    val blockY: Int
        get() = locToBlock(y)
    val blockZ: Int
        get() = locToBlock(z)

    fun getGroundDistanceTo(location: Location): Double {
        return sqrt((x - location.x).pow(2.0) + (z - location.z).pow(2.0))
    }

    fun getDistanceTo(location: Location): Double {
        return sqrt(
            (x - location.x).pow(2.0) + (y - location.y).pow(2.0) + (z - location.z).pow(2.0)
        )
    }

    operator fun plus(location: Location): Location {
        x += location.x
        y += location.y
        z += location.z
        return this
    }

    operator fun minus(location: Location): Location {
        x -= location.x
        y -= location.y
        z -= location.z
        return this
    }

    operator fun times(location: Location): Location {
        x *= location.x
        y *= location.y
        z *= location.z
        return this
    }

    operator fun div(location: Location): Location {
        x /= location.x
        y /= location.y
        z /= location.z
        return this
    }

    override fun toString(): String {
        return locationToString(this)
    }

    companion object {
        fun stringToLocation(string: String): Location {
            val split = string.split(", ")
            if (split.size < 4) {
                throw IllegalArgumentException()
            }
            val world = split[0]
            val x = split[1].toDouble()
            val y = split[2].toDouble()
            val z = split[3].toDouble()
            val customLocation = Location(world, x, y, z)
            if (split.size >= 5) {
                customLocation.yaw = split[3].toFloat()
                customLocation.pitch = split[4].toFloat()
            }
            return customLocation
        }

        fun locationToString(loc: Location): String {
            val joiner = StringJoiner(", ")
                .add(loc.worldName)
                .add(loc.x.toString())
                .add(loc.y.toString())
                .add(loc.z.toString())
            return if (loc.yaw == 0.0f && loc.pitch == 0.0f)
                joiner.toString()
            else {
                joiner
                    .add(loc.yaw.toString())
                    .add(loc.pitch.toString())
                    .toString()
            }
        }

        fun locToBlock(loc: Double): Int {
            return loc.floor()
        }
    }
}
