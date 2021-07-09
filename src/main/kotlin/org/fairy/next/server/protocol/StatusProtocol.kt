package org.fairy.next.org.fairy.next.server.protocol

import net.kyori.adventure.text.Component
import org.fairy.next.org.fairy.next.extension.mc
import org.fairy.next.org.fairy.next.server.packet.receive.PacketPing
import org.fairy.next.org.fairy.next.server.packet.receive.PacketStatusStart
import org.fairy.next.org.fairy.next.server.packet.send.PacketPong
import org.fairy.next.org.fairy.next.server.packet.send.PacketServerInfo
import org.fairy.next.org.fairy.next.server.ping.ServerPing
import org.fairy.next.org.fairy.next.server.ping.ServerSample
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.protocol.AbstractProtocol

class StatusProtocol : AbstractProtocol(1) {

    private var statusPerformed = false

    override fun register() {
        this.registerReceive(0x00, PacketStatusStart::class)
        this.registerSend(0x00, PacketServerInfo::class)
        this.registerReceive(0x01, PacketPing::class)
        this.registerSend(0x01, PacketPong::class)
    }

    override fun handleStatusStart(networkHandler: NetworkHandler, packet: PacketStatusStart) {
        if (this.statusPerformed) {
            networkHandler.close(Component.text("Status request has been handled."))
            return
        }
        this.statusPerformed = true

        val samples = ArrayList<ServerSample>()
        mc.playerStorage.forEach {
            val sample = ServerSample(it.uuid.toString(), it.name)
            samples += sample
        }

        // allow customizable
        val motd = Component.text("Next Generation of Minecraft Server")
        val favicon = null
        val serverName = "NextMC"
        val serverProtocol = 47
        val max = 100
        val online = 0

        val send = PacketServerInfo()
        send.serverInfo = ServerPing(
            motd,
            favicon,
            serverName,
            serverProtocol,
            max,
            online,
            samples
        )

        networkHandler.send(send)
    }

    override fun handlePing(networkHandler: NetworkHandler, packet: PacketPing) {
        // ping pong!
        val pong = PacketPong()
        pong.randomId = packet.randomId

        networkHandler.send(pong)
        networkHandler.close(Component.text("Status request has been handled."))
    }
}