package org.fairy.next.org.fairy.next.server.packet.receive

import io.netty.buffer.ByteBuf
import org.fairy.next.org.fairy.next.server.packet.ReceivePacket
import org.fairy.next.org.fairy.next.util.decryptData
import org.fairy.next.org.fairy.next.util.decryptToSecretKey
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.readByteArray
import java.security.PrivateKey
import javax.crypto.SecretKey

class PacketEncryptionResponse : ReceivePacket {

    lateinit var secretKeyEncrypted: ByteArray
    lateinit var verifyTokenEncrypted: ByteArray

    override fun decode(buf: ByteBuf) {
        this.secretKeyEncrypted = buf.readByteArray(256)
        this.verifyTokenEncrypted = buf.readByteArray(256)
    }

    override fun handle(networkHandler: NetworkHandler) {

    }

    fun decryptSecretKey(privateKey: PrivateKey) : SecretKey = decryptToSecretKey(privateKey, this.secretKeyEncrypted)

    fun decryptVerifyToken(privateKey: PrivateKey?) : ByteArray = privateKey?.let { decryptData(it, this.verifyTokenEncrypted) } ?: this.verifyTokenEncrypted
}