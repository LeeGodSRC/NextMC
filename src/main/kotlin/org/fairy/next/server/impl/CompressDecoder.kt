package org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.fairy.next.constant.MAXIMUM_UNCOMPRESSED_SIZE
import org.fairy.next.server.readVarInt
import org.fairy.next.server.util.checkFrame
import java.util.zip.Inflater

class CompressDecoder(var threshold: Int) : ByteToMessageDecoder() {

    private val compressor = Inflater()

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() == 0) {
            return
        }

        val claimedUncompressedSize = buf.readVarInt()
        if (claimedUncompressedSize == 0) {
            out.add(buf.retain())
        }

        checkFrame(claimedUncompressedSize >= threshold, "Uncompressed size %s is less than"
                + " threshold %s", claimedUncompressedSize, threshold);
        checkFrame(claimedUncompressedSize <= MAXIMUM_UNCOMPRESSED_SIZE,
            "Uncompressed size %s exceeds hard threshold of %s", claimedUncompressedSize,
            MAXIMUM_UNCOMPRESSED_SIZE);

        val compatibleIn = ByteArray(buf.readableBytes())
        buf.readBytes(compatibleIn)
        this.compressor.setInput(compatibleIn)

        val uncompressed = ByteArray(claimedUncompressedSize)
        this.compressor.inflate(uncompressed)
        out.add(Unpooled.wrappedBuffer(uncompressed))

        this.compressor.reset()
    }
}