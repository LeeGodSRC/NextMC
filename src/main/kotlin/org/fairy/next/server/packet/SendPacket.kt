package org.fairy.next.server.packet

import io.netty.buffer.ByteBuf
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.Packet

interface SendPacket : Packet {

    override fun decode(buf: ByteBuf) {
        throw UnsupportedOperationException("decode() in SendPacket")
    }

    override fun handle(networkHandler: NetworkHandler) {
        throw UnsupportedOperationException("handle() in SendPacket")
    }

}