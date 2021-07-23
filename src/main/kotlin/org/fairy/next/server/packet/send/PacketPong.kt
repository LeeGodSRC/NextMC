package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.SendPacket

class PacketPong : SendPacket {

    var randomId: Long = -1

    override fun encode(buf: ByteBuf) {
        buf.writeLong(randomId)
    }
}