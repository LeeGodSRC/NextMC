package org.fairy.next.org.fairy.next.server.impl

import io.netty.util.ByteProcessor
import kotlin.experimental.and

internal class VarintByteDecoder : ByteProcessor {
    var readVarint = 0
        private set
    var bytesRead = 0
        private set
    var result = DecodeResult.TOO_SHORT
        private set

    override fun process(k: Byte): Boolean {
        readVarint = readVarint or (k.toInt() and 0x7F shl bytesRead++ * 7)
        if (bytesRead > 3) {
            result = DecodeResult.TOO_BIG
            return false
        }
        if (k and 0x80.toByte() != 128.toByte()) {
            result = DecodeResult.SUCCESS
            return false
        }
        return true
    }

    enum class DecodeResult {
        SUCCESS, TOO_SHORT, TOO_BIG
    }
}
