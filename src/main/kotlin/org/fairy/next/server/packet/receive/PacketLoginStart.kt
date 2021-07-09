package org.fairy.next.org.fairy.next.server.packet.receive

import com.mojang.authlib.GameProfile
import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.readString

class PacketLoginStart : ReceivePacket {

    lateinit var gameProfile: GameProfile

    override fun decode(buf: ByteBuf) {
        this.gameProfile = GameProfile(null, buf.readString(16))
    }

    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol?.handleLoginStart(networkHandler, this)
    }

}