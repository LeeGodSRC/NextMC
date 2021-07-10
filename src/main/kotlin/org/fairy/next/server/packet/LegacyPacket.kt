package org.fairy.next.org.fairy.next.server.packet

import io.netty.buffer.ByteBuf
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.Packet

interface LegacyPacket : Packet {
    override fun decode(buf: ByteBuf) {
        throw UnsupportedOperationException()
    }

    override fun encode(buf: ByteBuf) {
        throw UnsupportedOperationException()
    }
}