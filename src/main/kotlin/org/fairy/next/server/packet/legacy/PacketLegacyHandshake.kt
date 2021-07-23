package org.fairy.next.server.packet.legacy

import org.fairy.next.server.packet.LegacyPacket
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.Protocol

class PacketLegacyHandshake : LegacyPacket {
    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handleLegacyHandshake(networkHandler, this)
    }

    override fun protocol(): Protocol {
        return Protocol.HANDSHAKE
    }
}