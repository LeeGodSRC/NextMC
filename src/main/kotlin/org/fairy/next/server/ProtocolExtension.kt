package org.fairy.next.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.CorruptedFrameException
import net.kyori.adventure.text.Component
import org.fairy.next.constant.DEFAULT_MAX_STRING_SIZE
import org.fairy.next.extension.readComponentFromJson
import org.fairy.next.extension.toJsonObject
import org.fairy.next.server.ping.gson
import org.fairy.next.server.util.checkFrame
import org.fairy.next.util.Int3
import java.io.Closeable
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ceil

private val packetCache = IdentityHashMap<ByteBuf, PacketInfo>()
private val VARINT_EXACT_BYTE_LENGTHS: IntArray by lazy {
        val value = IntArray(33)
        repeat (32) {
            value[it] = ceil((31.0 -(it - 1)) / 7.0).toInt()
        }
        value[32] = 1
        value
    }

data class PacketInfo(val protocol: Int, var locale: Locale)

fun varIntBytes(value: Int): Int = VARINT_EXACT_BYTE_LENGTHS[value.countLeadingZeroBits()]

fun ByteBuf.cache(packetInfo: PacketInfo): Closeable {
    packetCache[this] = packetInfo
    return Closeable { packetCache[this] = null }
}

var ByteBuf.locale: Locale
    get() = packetCache.get(this)?.locale ?: Locale.US
    set(value) {
        packetCache.get(this)?.let { packetInfo -> packetInfo.locale = value }
    }

fun ByteBuf.writeUuid(uuid: UUID): ByteBuf {
    this.writeLong(uuid.mostSignificantBits)
    this.writeLong(uuid.leastSignificantBits)
    return this
}

fun <T> ByteBuf.writeCollection(collection: Array<T>, transform: (T) -> Unit): ByteBuf {
    return this.writeCollection(collection.asList(), transform)
}

fun <T> ByteBuf.writeCollection(collection: Collection<T>, transform: (T) -> Unit): ByteBuf {
    this.writeVarInt(collection.size)
    collection.forEach { transform.invoke(it) }
    return this
}

fun ByteBuf.writeEnum(enum: Enum<*>): ByteBuf {
    this.writeVarInt(enum.ordinal)
    return this
}

fun ByteBuf.writeInt3(pos: Int3): ByteBuf {
    this.writeLong(pos.asLong())
    return this
}

fun ByteBuf.writeComponent(component: Component): ByteBuf {
    this.writeString(gson.toJson(component.toJsonObject(this.locale)))
    return this
}

fun ByteBuf.readComponent(): Component {
    return readComponentFromJson(this.readString())
}

/**
 * Reads a VarInt length-prefixed byte array from the `buf`, making sure to not go over
 * `cap` size.
 * @param buf the buffer to read from
 * @param cap the maximum size of the string, in UTF-8 character length
 * @return the byte array
 */
fun ByteBuf.readByteArray(cap: Int): ByteArray {
    val length: Int = this.readVarInt()
    checkFrame(length >= 0, "Got a negative-length array (%s)", length)
    checkFrame(length <= cap, "Bad array size (got %s, maximum is %s)", length, cap)
    checkFrame(
        this.isReadable(length),
        "Trying to read an array that is too long (wanted %s, only have %s)", length,
        this.readableBytes()
    )
    val array = ByteArray(length)
    this.readBytes(array)
    return array
}

fun ByteBuf.writeByteArray(array: ByteArray) {
    writeVarInt(array.size)
    this.writeBytes(array)
}

/**
 * Reads a Minecraft-style VarInt from the specified `buf`.
 * @return the decoded VarInt
 */
fun ByteBuf.readVarInt(): Int {
    val read = this.readVarIntSafely()
    if (read == Int.MIN_VALUE) {
        throw CorruptedFrameException("Bad VarInt decoded")
    }
    return read
}

/**
 * Reads a Minecraft-style VarInt from the specified `buf`. The difference between this
 * method and [.readVarInt] is that this function returns a sentinel value if the
 * varint is invalid.
 * @return the decoded VarInt, or `Integer.MIN_VALUE` if the varint is invalid
 */
fun ByteBuf.readVarIntSafely(): Int {
    var i = 0
    val maxRead = 5.coerceAtMost(this.readableBytes())
    for (j in 0 until maxRead) {
        val k = this.readByte().toInt()
        i = i or (k and 0x7F shl j * 7)
        if (k and 0x80 != 128) {
            return i
        }
    }
    return Int.MIN_VALUE
}

/**
 * Writes a Minecraft-style VarInt to the specified `buf`.
 * @param buf the buffer to read from
 * @param value the integer to write
 */
fun ByteBuf.writeVarInt(value: Int) {
    // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
    // that the proxy will write, to improve inlining.
    if (value and (-0x1 shl 7) == 0) {
        this.writeByte(value)
    } else if (value and (-0x1 shl 14) == 0) {
        val w = value and 0x7F or 0x80 shl 8 or (value ushr 7)
        this.writeShort(w)
    } else {
        this.writeVarIntFull(value)
    }
}

private fun ByteBuf.writeVarIntFull(value: Int) {
    // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
    if (value and (-0x1 shl 7) == 0) {
        this.writeByte(value)
    } else if (value and (-0x1 shl 14) == 0) {
        val w = value and 0x7F or 0x80 shl 8 or (value ushr 7)
        this.writeShort(w)
    } else if (value and (-0x1 shl 21) == 0) {
        val w = value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14)
        this.writeMedium(w)
    } else if (value and (-0x1 shl 28) == 0) {
        val w = (value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16)
                or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21))
        this.writeInt(w)
    } else {
        val w = value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16
                ) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80)
        this.writeInt(w)
        this.writeByte(value ushr 28)
    }
}

/**
 * Writes the specified `value` as a 21-bit Minecraft VarInt to the specified `buf`.
 * The upper 11 bits will be discarded.
 * @param buf the buffer to read from
 * @param value the integer to write
 */
fun ByteBuf.write21BitVarInt(value: Int) {
    // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
    val w = value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14)
    this.writeMedium(w)
}

fun ByteBuf.readString(): String {
    return this.readString(DEFAULT_MAX_STRING_SIZE)
}

/**
 * Reads a VarInt length-prefixed UTF-8 string from the `buf`, making sure to not go over
 * `cap` size.
 * @param buf the buffer to read from
 * @param cap the maximum size of the string, in UTF-8 character length
 * @return the decoded string
 */
fun ByteBuf.readString(cap: Int): String {
    val length: Int = this.readVarInt()
    return this.readString(cap, length)
}

private fun ByteBuf.readString(cap: Int, length: Int): String {
    checkFrame(length >= 0, "Got a negative-length string (%s)", length)
    // `cap` is interpreted as a UTF-8 character length. To cover the full Unicode plane, we must
    // consider the length of a UTF-8 character, which can be up to 4 bytes. We do an initial
    // sanity check and then check again to make sure our optimistic guess was good.
    checkFrame(length <= cap * 4, "Bad string size (got %s, maximum is %s)", length, cap)
    checkFrame(
        this.isReadable(length),
        "Trying to read a string that is too long (wanted %s, only have %s)", length,
        this.readableBytes()
    )
    val str = this.toString(this.readerIndex(), length, StandardCharsets.UTF_8)
    this.skipBytes(length)
    checkFrame(
        str.length <= cap, "Got a too-long string (got %s, max %s)",
        str.length, cap
    )
    return str
}

/**
 * Writes the specified `str` to the `buf` with a VarInt prefix.
 * @param buf the buffer to write to
 * @param str the string to write
 */
fun ByteBuf.writeString(str: CharSequence?) {
    val size = ByteBufUtil.utf8Bytes(str)
    this.writeVarInt(size)
    this.writeCharSequence(str, StandardCharsets.UTF_8)
}