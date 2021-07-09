package org.fairy.next.org.fairy.next.util

import org.fairy.next.org.fairy.next.extension.mc
import org.fairy.next.world.World
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

data class Location(
    var worldName: String,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0.0f,
    var pitch: Float = 0.0f
) {

    constructor(worldName: String, x: Int, y: Int, z: Int, yaw: Float = 0.0f, pitch: Float = 0.0f): this(
        worldName,
        x.toDouble(),
        y.toDouble(),
        z.toDouble(),
        yaw,
        pitch
    )

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
