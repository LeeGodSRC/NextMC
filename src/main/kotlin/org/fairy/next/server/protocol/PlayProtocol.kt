package org.fairy.next.server.protocol

import org.fairy.next.extension.curTime
import org.fairy.next.extension.mc
import org.fairy.next.org.fairy.next.server.packet.send.PacketTeleport
import org.fairy.next.server.packet.both.PacketKeepAlive
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.send.*
import org.fairy.next.server.protocol.AbstractProtocol

class PlayProtocol : AbstractProtocol(0) {
    override fun register() {
        this.registerSend(0x00, PacketKeepAlive::class)
        this.registerSend(0x01, PacketJoinGame::class)
        this.registerSend(0x02, PacketMessage::class)
        this.registerSend(0x03, PacketUpdateTime::class)

        this.registerSend(0x05, PacketSpawnPos::class)


        this.registerSend(0x08, PacketTeleport::class)

        this.registerSend(0x38, PacketPlayerInfo::class)
        this.registerSend(0x40, PacketDisconnect::class)

        this.registerReceive(0x00, PacketKeepAlive::class)
    }

    override fun disconnect(networkHandler: NetworkHandler) {
        mc.playerStorage.disconnect(networkHandler.player)
    }

    override fun handleKeepAlive(networkHandler: NetworkHandler, randomId: Long) {
        if (networkHandler.keepAliveId != randomId)
            return

        val diff = curTime - networkHandler.keepAliveTime
        networkHandler.ping = (networkHandler.ping * 3 + diff) / 4
    }
}