package org.fairy.next.server.packet.receive

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.player.Player
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.Packet

class PacketStatusStart : ReceivePacket {
    override fun decode(buf: ByteBuf) {
    }

    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol!!.handleStatusStart(networkHandler, this)
    }
}