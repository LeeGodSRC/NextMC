package org.fairy.next.org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.SendPacket
import org.fairy.next.org.fairy.next.server.ping.ServerPing
import org.fairy.next.server.locale
import org.fairy.next.server.writeString

class PacketServerInfo : SendPacket {

    lateinit var serverInfo: ServerPing

    override fun encode(buf: ByteBuf) {
        buf.writeString(serverInfo.toJsonString(buf.locale))
    }

}