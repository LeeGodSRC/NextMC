package org.fairy.next.org.fairy.next.server.protocol

import com.google.common.base.Preconditions
import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import org.fairy.next.extension.logger
import org.fairy.next.org.fairy.next.extension.mc
import org.fairy.next.org.fairy.next.server.packet.receive.PacketEncryptionResponse
import org.fairy.next.org.fairy.next.server.packet.send.PacketEncryptionRequest
import org.fairy.next.org.fairy.next.server.packet.receive.PacketLoginStart
import org.fairy.next.org.fairy.next.server.packet.send.PacketLoginSuccess
import org.fairy.next.org.fairy.next.server.packet.send.PacketSetCompression
import org.fairy.next.org.fairy.next.util.getServerIdHash
import org.fairy.next.player.Player
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.send.PacketDisconnect
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.thread.newThread
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import kotlin.IllegalStateException

class LoginProtocol : AbstractProtocol(2) {

    private val random = Random()
    private val serverId = ""

    override fun register() {
        this.registerSend(0, PacketDisconnect::class)
        this.registerSend(1, PacketEncryptionRequest::class)
        this.registerSend(2, PacketLoginSuccess::class)
        this.registerSend(3, PacketSetCompression::class)

        this.registerReceive(0, PacketLoginStart::class)
        this.registerReceive(1, PacketEncryptionResponse::class)
    }

    override fun handleLoginStart(networkHandler: NetworkHandler, packet: PacketLoginStart) {
        networkHandler.gameProfile = packet.gameProfile

        if (mc.onlineMode && !networkHandler.isLocalChannel()) {
            networkHandler.loginProgress = Progress.KEY
            networkHandler.loginToken = ByteArray(4)
            this.random.nextBytes(networkHandler.loginToken)

            val encryptionRequest = PacketEncryptionRequest()
            encryptionRequest.serverId = this.serverId
            encryptionRequest.publicKey = mc.keyPair.public
            encryptionRequest.verifyToken = networkHandler.loginToken!!

            networkHandler.send(encryptionRequest)
        } else {
            // offline UUID
            val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:${networkHandler.gameProfile.name}".toByteArray(Charsets.UTF_8))
            networkHandler.gameProfile = GameProfile(uuid, networkHandler.gameProfile.name)

            newThread("Authenticator Thread") {
                this.prepareConnectionAsync(networkHandler)
            }
        }
    }

    override fun handleEncryptionResponse(networkHandler: NetworkHandler, packet: PacketEncryptionResponse) {
        Preconditions.checkArgument(networkHandler.loginProgress == Progress.KEY, "Unexpected key packet")
        val privateKey = mc.keyPair.private

        if (!Arrays.equals(networkHandler.loginToken, packet.decryptVerifyToken(privateKey))) {
            throw IllegalStateException("Invalid token!")
        }

        networkHandler.loginKey = packet.decryptSecretKey(privateKey)
        networkHandler.loginProgress = Progress.AUTHENTICATING
        newThread("Authenticator Thread") {
            val gameProfile = networkHandler.gameProfile

            val s =
                BigInteger(getServerIdHash(serverId, mc.keyPair.public, networkHandler.loginKey!!)).toString(16)
            mc.minecraftSessionService.hasJoinedServer(
                GameProfile(null, gameProfile.name),
                s,
                getINetAddress(networkHandler)
            )?.let {
                networkHandler.gameProfile = it
                if (!networkHandler.isOpen())
                    return@newThread

                this.prepareConnectionAsync(networkHandler)
            }
        }
    }

    private fun prepareConnectionAsync(networkHandler: NetworkHandler) {
        val playerName = networkHandler.gameProfile.name
        val address = getINetAddress(networkHandler)!!
        val uuid = networkHandler.gameProfile.id

        // allow async event

        logger().info("UUID of the Player $playerName is $uuid")
        networkHandler.loginProgress = Progress.PRE_ACCEPT

        this.loginAsync(networkHandler, networkHandler.host!!)
    }

    /**
     * Login, must be thread safe
     */
    fun loginAsync(networkHandler: NetworkHandler, host: String) {
        val uuid = networkHandler.gameProfile.id

        // logout same guy
        mc.playerStorage.forEach {
            if (uuid != it.uuid) {
                return@forEach
            }

            it.disconnect(Component.text("You logged from another location"))
        }

        val player = Player(networkHandler)

        // TODO logging event
        // TODO ban, whitelist, max players

        // mark as accepted
        networkHandler.loginProgress = Progress.ACCEPTED
        val compressionThreshold = mc.networkCompressionThreshold

        if (compressionThreshold >= 0 && !networkHandler.isLocalChannel()) {
            val setCompression = PacketSetCompression()
            setCompression.networkCompressionThreshold = compressionThreshold

            networkHandler.send(setCompression) {
                networkHandler.setupCompression(compressionThreshold)
            }
        }

        val loginSuccess = PacketLoginSuccess()
        loginSuccess.gameProfile = networkHandler.gameProfile

        networkHandler.send(loginSuccess)
        mc.playerStorage.processLogin(networkHandler, player)
    }

    fun getINetAddress(networkHandler: NetworkHandler): InetAddress? {
        return networkHandler.address?.let { return if (it is InetSocketAddress) it.address else null }
    }

    enum class Progress {

        HELLO, KEY, AUTHENTICATING, PRE_ACCEPT, ACCEPTED

    }
}