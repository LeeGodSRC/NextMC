package org.fairy.next.server.packet.send

import com.mojang.authlib.GameProfile
import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.writeString

class PacketLoginSuccess : SendPacket {

    lateinit var gameProfile: GameProfile

    override fun encode(buf: ByteBuf) {
        buf.writeString(gameProfile.id?.toString() ?: "")
        buf.writeString(gameProfile.name)
    }
}