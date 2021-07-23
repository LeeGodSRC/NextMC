package org.fairy.next.server.packet.legacy

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.LegacyPacket
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.Protocol
import org.fairy.next.server.util.LegacyMinecraftPingVersion
import org.fairy.next.server.packet.Packet
import java.net.InetSocketAddress

class PacketLegacyPing(val version: LegacyMinecraftPingVersion, val vhost: InetSocketAddress?) : LegacyPacket {
    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handleLegacyPing(networkHandler, this)
    }

    override fun protocol(): Protocol {
        return Protocol.HANDSHAKE
    }

}