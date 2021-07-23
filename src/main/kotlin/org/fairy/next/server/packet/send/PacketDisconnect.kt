package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import net.kyori.adventure.text.Component
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.player.Player
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.readComponent
import org.fairy.next.server.writeComponent
import org.fairy.next.server.writeString
import org.fairy.next.server.packet.Packet

class PacketDisconnect : SendPacket {

    lateinit var message: Component

    override fun encode(buf: ByteBuf) {
        buf.writeComponent(this.message)
    }
}