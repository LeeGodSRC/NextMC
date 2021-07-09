package org.fairy.next.org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.writeVarInt

class PacketSetCompression : SendPacket {

    var networkCompressionThreshold: Int = -1

    override fun encode(buf: ByteBuf) {
        buf.writeVarInt(this.networkCompressionThreshold)
    }
}