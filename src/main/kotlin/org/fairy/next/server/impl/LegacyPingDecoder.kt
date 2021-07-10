package org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.fairy.next.org.fairy.next.server.packet.legacy.PacketLegacyHandshake
import org.fairy.next.server.util.LegacyMinecraftPingVersion
import org.fairy.next.server.util.checkFrame
import org.fairy.next.org.fairy.next.server.packet.legacy.PacketLegacyPing
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets


class LegacyPingDecoder : ByteToMessageDecoder() {

    companion object {
        val MC_1_6_CHANNEL : String = "MC|PingHost";
        private fun readExtended16Data(`in`: ByteBuf): PacketLegacyPing {
            `in`.skipBytes(1)
            val channelName = readLegacyString(`in`)
            require(channelName == MC_1_6_CHANNEL) { "Didn't find correct channel" }
            `in`.skipBytes(3)
            val hostname = readLegacyString(`in`)
            val port = `in`.readInt()
            return PacketLegacyPing(
                LegacyMinecraftPingVersion.MINECRAFT_1_6, InetSocketAddress
                    .createUnresolved(hostname, port)
            )
        }

        private fun readLegacyString(buf: ByteBuf): String {
            val len = buf.readShort() * Character.BYTES
            checkFrame(
                buf.isReadable(len), "String length %s is too large for available bytes %d",
                len, buf.readableBytes()
            )
            val str = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_16BE)
            buf.skipBytes(len)
            return str
        }
    }

    override fun decode(ctx: ChannelHandlerContext?, data: ByteBuf?, out: MutableList<Any>?) {
        data ?: throw IllegalArgumentException()
        ctx ?: throw IllegalArgumentException()
        out ?: throw IllegalArgumentException()

        if (!data.isReadable) return
        if (!ctx.channel().isActive) {
            data.clear()
            return
        }

        val socketAddress = ctx.channel().remoteAddress() as InetSocketAddress
        val originalReaderIndex: Int = data.readerIndex()
        val first: Short = data.readUnsignedByte()
        if (first.toInt() == 0xfe) {
            // possibly a ping
            if (!data.isReadable()) {
                out.add(PacketLegacyPing(LegacyMinecraftPingVersion.MINECRAFT_1_3, socketAddress))
                return
            }
            val next: Short = data.readUnsignedByte()
            if (next.toInt() == 1 && !data.isReadable()) {
                out.add(PacketLegacyPing(LegacyMinecraftPingVersion.MINECRAFT_1_4, socketAddress))
                return
            }

            // We got a 1.6.x ping. Let's chomp off the stuff we don't need.
            out.add(readExtended16Data(data))
        } else if (first.toInt() == 0x02 && data.isReadable()) {
            data.skipBytes(data.readableBytes())
            out.add(PacketLegacyHandshake())
        } else {
            data.readerIndex(originalReaderIndex)
            ctx.pipeline().remove(this)
        }
    }
}