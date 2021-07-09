package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.util.LegacyMinecraftPingVersion
import org.fairy.next.server.packet.Packet
import java.net.InetSocketAddress

class PacketLegacyPing(val version: LegacyMinecraftPingVersion, val vhost: InetSocketAddress?) : SendPacket {

    override fun encode(buf: ByteBuf) {

    }

}