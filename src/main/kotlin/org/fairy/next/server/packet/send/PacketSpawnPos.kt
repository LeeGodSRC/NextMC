package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.writeInt3
import org.fairy.next.util.Int3

class PacketSpawnPos(val pos: Int3): SendPacket {

    override fun encode(buf: ByteBuf) {
        buf.writeInt3(pos)
    }

}