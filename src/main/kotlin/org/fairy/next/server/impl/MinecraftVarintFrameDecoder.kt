package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf

import io.netty.channel.ChannelHandlerContext

import io.netty.handler.codec.ByteToMessageDecoder


class MinecraftVarintFrameDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!ctx.channel().isActive) {
            buf.clear()
            return
        }
        val reader = VarintByteDecoder()
        val varintEnd = buf.forEachByte(reader)
        if (varintEnd == -1) {
            // We tried to go beyond the end of the buffer. This is probably a good sign that the
            // buffer was too short to hold a proper varint.
            return
        }
        if (reader.result === VarintByteDecoder.DecodeResult.SUCCESS) {
            val readVarint: Int = reader.readVarint
            val bytesRead: Int = reader.bytesRead
            if (readVarint < 0) {
                buf.clear()
                throw BAD_LENGTH_CACHED
            } else if (readVarint == 0) {
                // skip over the empty packet and ignore it
                buf.readerIndex(varintEnd + 1)
            } else {
                val minimumRead = bytesRead + readVarint
                if (buf.isReadable(minimumRead)) {
                    out.add(buf.retainedSlice(varintEnd + 1, readVarint))
                    buf.skipBytes(minimumRead)
                }
            }
        } else if (reader.result === VarintByteDecoder.DecodeResult.TOO_BIG) {
            buf.clear()
            throw VARINT_BIG_CACHED
        }
    }

    companion object {
        private val BAD_LENGTH_CACHED = RuntimeException("Bad packet length")
        private val VARINT_BIG_CACHED = RuntimeException("VarInt too big")
    }
}