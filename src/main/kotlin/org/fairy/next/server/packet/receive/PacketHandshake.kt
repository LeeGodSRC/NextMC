package org.fairy.next.server.packet.receive

import io.netty.buffer.ByteBuf
import org.fairy.next.constant.MAXIMUM_HOSTNAME_LENGTH
import org.fairy.next.org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.player.Player
import org.fairy.next.server.*
import org.fairy.next.server.packet.Packet

open class PacketHandshake : ReceivePacket {

    var version: Int = -1
    var address: String = "none"
    var serverPort: Short = -1
    var nextState = Protocol.HANDSHAKE

    override fun decode(buf: ByteBuf) {
        this.version = buf.readVarInt()
        this.address = buf.readString(MAXIMUM_HOSTNAME_LENGTH)
        this.serverPort = buf.readUnsignedShort().toShort()
        this.nextState = Protocol.findProtocol(buf.readVarInt())
    }

    override fun handle(networkHandler: NetworkHandler) {
        networkHandler.protocol!!.handleHandshake(networkHandler, this)
    }
}