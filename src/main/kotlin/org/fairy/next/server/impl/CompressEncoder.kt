package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.fairy.next.server.writeVarInt
import java.util.zip.Deflater

class CompressEncoder(var threshold: Int) : MessageToByteEncoder<ByteBuf>() {

    val compressingBytes = ByteArray(8192)
    val compressor = Deflater()

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        val uncompressed = msg.readableBytes()
        if (uncompressed < threshold) {
            out.writeVarInt(0)
            out.writeBytes(msg)
        } else {
            val byteArray = ByteArray(uncompressed)
            msg.readBytes(byteArray)
            out.writeVarInt(uncompressed)
            this.compressor.setInput(byteArray, 0, uncompressed)
            this.compressor.finish()

            while (!this.compressor.finished()) {
                val size = this.compressor.deflate(compressingBytes)
                out.writeBytes(compressingBytes, 0, size)
            }

            this.compressor.reset()
        }
    }
}