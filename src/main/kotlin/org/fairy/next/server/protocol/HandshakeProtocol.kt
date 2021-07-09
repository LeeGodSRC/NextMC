package org.fairy.next.server.protocol

import org.fairy.next.org.fairy.next.server.protocol.LoginProtocol
import org.fairy.next.player.Player
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.Protocol
import org.fairy.next.server.packet.receive.PacketHandshake

class HandshakeProtocol : AbstractProtocol(-1) {

    override fun register() {
        registerReceive(0x00, PacketHandshake::class)
    }

    override fun handleHandshake(networkHandler: NetworkHandler, packet: PacketHandshake) {
        when (packet.nextState) {
            (Protocol.LOGIN) -> {
                networkHandler.protocol = packet.nextState.protocol
                networkHandler.loginProgress = LoginProtocol.Progress.HELLO
                networkHandler.host = packet.address + ":" + packet.serverPort
                // TODO - check protocol, bungee mode and throttle
                return
            }
            (Protocol.STATUS) -> {
                networkHandler.protocol = packet.nextState.protocol
                return
            }
            else -> throw UnsupportedOperationException()
        }
    }
}