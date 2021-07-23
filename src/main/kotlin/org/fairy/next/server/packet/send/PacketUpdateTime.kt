package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.SendPacket

class PacketUpdateTime(private val worldTime: Long, val playerTime: Long): SendPacket {
    override fun encode(buf: ByteBuf) {
        buf.writeLong(this.worldTime)
        buf.writeLong(this.playerTime)
    }
}