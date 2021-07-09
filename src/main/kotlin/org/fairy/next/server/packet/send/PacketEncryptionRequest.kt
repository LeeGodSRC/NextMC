package org.fairy.next.org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.org.fairy.next.server.packet.SendPacket
import org.fairy.next.server.*
import org.fairy.next.server.util.decodePublicKey
import java.security.PublicKey

class PacketEncryptionRequest : SendPacket {

    lateinit var serverId: String
    lateinit var publicKey: PublicKey
    lateinit var verifyToken: ByteArray

    override fun encode(buf: ByteBuf) {
        buf.writeString(this.serverId)
        buf.writeByteArray(this.publicKey.encoded)
        buf.writeByteArray(this.verifyToken)
    }
}