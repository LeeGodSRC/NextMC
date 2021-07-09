package org.fairy.next.org.fairy.next.util

import net.jafama.FastMath

fun Double.floor(): Int {
    return FastMath.floorToInt(this)
}

fun Float.floor(): Int {
    return FastMath.floorToInt(this.toDouble())
}

fun toHash(x: Int, z: Int): Long {
    return (x.toLong() shl 32) + z - Int.MIN_VALUE
}

fun fromHash(hash: Long): ChunkCoords {
    val x = (hash shr 32).toInt()
    val z = (hash and -0x1).toInt() + Int.MIN_VALUE
    return ChunkCoords(x, z)
}

data class ChunkCoords(var x: Int, var z: Int) {
    fun hash(): Long = toHash(x, z)

}