package org.fairy.next.server.packet.receive

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.server.NetworkHandler

class PacketPing : ReceivePacket {

    var randomId: Long = -1

    override fun decode(buf: ByteBuf) {
        this.randomId = buf.readLong()
    }

    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handlePing(networkHandler, this)
    }
}