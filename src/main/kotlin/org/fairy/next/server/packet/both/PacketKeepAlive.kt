package org.fairy.next.server.packet.both

import io.netty.buffer.ByteBuf
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.Packet
import org.fairy.next.server.readVarInt
import org.fairy.next.server.writeVarInt

class PacketKeepAlive : Packet {

    var id: Long = -1

    override fun decode(buf: ByteBuf) {
        id = buf.readVarInt().toLong()
    }

    override fun encode(buf: ByteBuf) {
        buf.writeVarInt(this.id.toInt())
    }

    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handleKeepAlive(networkHandler, id)
    }
}