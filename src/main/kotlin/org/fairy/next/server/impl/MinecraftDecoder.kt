package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.CorruptedFrameException
import org.fairy.next.extension.log
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.Packet
import org.fairy.next.server.readVarInt
import java.lang.RuntimeException
import kotlin.Any
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.let
import kotlin.run


class MinecraftDecoder(val networkHandler: NetworkHandler): ChannelInboundHandlerAdapter() {

    companion object {
        val DEBUG = System.getProperty("packet-decode-logging", "false").toBoolean()
        private val DECODE_FAILED = RuntimeException(
            "A packet did not decode successfully (invalid data). If you are a "
                    + "developer, launch Velocity with -Dvelocity.packet-decode-logging=true to see more."
        )
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg!! is ByteBuf) {
            val buf = msg as ByteBuf;
            if (!ctx!!.channel().isActive || !buf.isReadable) {
                buf.release()
                return
            }

            val originalReaderIndex = buf.readerIndex()
            val packetId = buf.readVarInt()

            val packet = this.networkHandler.protocol!!.createReceivePacket(packetId)
            packet?. let {
                try {
                    try {
                        it.decode(buf)
                    } catch (e: Exception) {
                        this.handleDecodeFailure(e, packet, packetId)
                    }

                    if (buf.isReadable) {
                        TODO("Overflow")
                    }

                    ctx.fireChannelRead(packet)
                } finally {
                    buf.release()
                }
            }?: run {
                buf.readerIndex(originalReaderIndex)
                ctx.fireChannelRead(buf)
            }
        } else {
            ctx!!.fireChannelRead(msg)
        }
    }

    private fun handleDecodeFailure(cause: Exception, packet: Packet, packetId: Int): Exception {
        return if (DEBUG) {
            CorruptedFrameException(
                "Error decoding " + packet::class.toString() + " " + getExtraConnectionDetail(
                    packetId
                ), cause
            )
        } else {
            DECODE_FAILED
        }
    }

    private fun getExtraConnectionDetail(packetId: Int): String {
        return "Direction Send Protocol ${networkHandler.version} Protocol ID " + Integer.toHexString(packetId)
    }

}