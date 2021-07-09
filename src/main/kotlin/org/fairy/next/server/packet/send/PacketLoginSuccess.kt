package org.fairy.next.org.fairy.next.server.packet.send

import com.mojang.authlib.GameProfile
import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.writeString

class PacketLoginSuccess : SendPacket {

    lateinit var gameProfile: GameProfile

    override fun encode(buf: ByteBuf) {
        var id = gameProfile.id
        buf.writeString(id?.toString() ?: "")
        buf.writeString(gameProfile.name)
    }
}