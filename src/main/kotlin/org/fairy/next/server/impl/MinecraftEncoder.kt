package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.fairy.next.extension.log
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.PacketInfo
import org.fairy.next.server.cache
import org.fairy.next.server.packet.Packet
import org.fairy.next.server.writeVarInt
import java.util.*

class MinecraftEncoder(private val networkHandler: NetworkHandler): MessageToByteEncoder<Packet>() {
    override fun encode(ctx: ChannelHandlerContext?, msg: Packet?, out: ByteBuf?) {
        val packetInfo = PacketInfo(networkHandler.version, Locale.US)

        try {
            out!!.cache(packetInfo).use {
                val protocol = msg!!.protocol()?.protocol ?: networkHandler.protocol!!
                out.writeVarInt(protocol.getPacketId(msg)!!)
                msg.encode(out)
            }
        } catch (ex: Exception) {
            log.error("An exception occurs while processing encode for packet ${msg!!::class.simpleName} while in protocol ${networkHandler.protocol!!::class.simpleName}", ex)
        }
    }


}