package org.fairy.next.server.packet

import io.netty.buffer.ByteBuf
import org.fairy.next.player.Player
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.Protocol

interface Packet {

    fun decode(buf: ByteBuf)

    fun encode(buf: ByteBuf)

    fun handle(networkHandler: NetworkHandler)

    fun protocol(): Protocol? {
        return null
    }

}