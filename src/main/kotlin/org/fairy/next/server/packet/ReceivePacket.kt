package org.fairy.next.org.fairy.next.server.packet

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.Packet

interface ReceivePacket : Packet {

    override fun encode(buf: ByteBuf) {
        throw UnsupportedOperationException("encode() in Receiving Packet")
    }

}