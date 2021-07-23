package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.fairy.next.server.varIntBytes
import org.fairy.next.server.writeVarInt


@Sharable
class MinecraftVarintLengthEncoder private constructor() : MessageToByteEncoder<ByteBuf>() {

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        out.writeVarInt(msg.readableBytes())
        out.writeBytes(msg)
    }

    override fun allocateBuffer(ctx: ChannelHandlerContext, msg: ByteBuf, preferDirect: Boolean): ByteBuf {
        val anticipatedRequiredCapacity: Int = (varIntBytes(msg.readableBytes())
                + msg.readableBytes())
        return ctx.alloc().directBuffer(anticipatedRequiredCapacity)
    }

    companion object {
        val INSTANCE = MinecraftVarintLengthEncoder()
    }
}