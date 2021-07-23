package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import net.kyori.adventure.text.Component
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.writeComponent

class PacketMessage(val component: Component, val type: Int): SendPacket {

    override fun encode(buf: ByteBuf) {
        buf.writeComponent(this.component)
        buf.writeByte(this.type)
    }
}