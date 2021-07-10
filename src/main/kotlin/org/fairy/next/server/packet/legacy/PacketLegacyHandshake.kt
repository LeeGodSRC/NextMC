package org.fairy.next.org.fairy.next.server.packet.legacy

import org.fairy.next.org.fairy.next.server.packet.LegacyPacket
import org.fairy.next.server.NetworkHandler

class PacketLegacyHandshake : LegacyPacket {
    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handleLegacyHandshake(networkHandler, this)
    }
}