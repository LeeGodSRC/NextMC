package org.fairy.next.server

import com.mojang.authlib.GameProfile
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.local.LocalChannel
import io.netty.channel.local.LocalServerChannel
import io.netty.util.concurrent.GenericFutureListener
import net.kyori.adventure.text.Component
import org.fairy.next.constant.HEARTBEAT_TIME
import org.fairy.next.extension.curTime
import org.fairy.next.org.fairy.next.server.impl.CompressDecoder
import org.fairy.next.org.fairy.next.server.impl.CompressEncoder
import org.fairy.next.org.fairy.next.server.protocol.LoginProtocol
import org.fairy.next.player.Player
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.server.packet.Packet
import java.net.SocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.crypto.SecretKey
import kotlin.concurrent.write

class NetworkHandler : SimpleChannelInboundHandler<Packet>() {

    val packetQueue = ConcurrentLinkedQueue<PendingPacket>()
    val lock = ReentrantReadWriteLock()

    lateinit var gameProfile: GameProfile

    lateinit var player: Player
    var protocol: AbstractProtocol? = null
        set(value) {
            this.channel?.let {
                it.attr(NettyServer.PROTOCOL_ATTRIBUTE).set(value)
                it.config().setAutoRead(true)
            }
            field = value
        }

    private var channel: Channel? = null
    private var disconnectMessage: Component? = null
    var address: SocketAddress? = null
    var host: String? = null

    val createdTimestamp = curTime

    var loginProgress: LoginProtocol.Progress = LoginProtocol.Progress.KEY
    var loginToken: ByteArray? = null
    var loginKey: SecretKey? = null

    set(value) {
        field?.run { throw IllegalStateException("Already registered encryption!") }
        value?.let {
            TODO()
            this.channel?.pipeline()?.addBefore("splitter", "decrypt", null)
            this.channel?.pipeline()?.addBefore("prepender", "encrypt", null)
        }
        field = value
    }

    fun send(packet: Packet) = this.lock.write {
        this.packetQueue += PendingPacket(packet, null)
    }

    fun send(packet: Packet, listener: GenericFutureListener<*>?) = this.lock.write {
        this.packetQueue += PendingPacket(packet, listener)
    }

    fun isLocalChannel(): Boolean = this.channel is LocalChannel || this.channel is LocalServerChannel

    fun setupCompression(threshold: Int) {
        val pipeline = this.channel!!.pipeline()!!

        if (threshold >= 0) {
            val originalDecompress = pipeline.get("decompress")
            if (originalDecompress is CompressDecoder) {
                originalDecompress.threshold = threshold
            } else {
                pipeline.addBefore("decoder", "decompress", CompressDecoder(threshold))
            }

            val originalCompress = pipeline.get("compress")
            if (originalCompress is CompressEncoder) {
                originalCompress.threshold = threshold
            } else {
                pipeline.addBefore("encoder", "compress", CompressEncoder(threshold))
            }
        } else {
            if (pipeline.get("decompress") is CompressDecoder) {
                pipeline.remove("decompress")
            }

            if (pipeline.get("compress") is CompressEncoder) {
                pipeline.remove("decompress")
            }
        }
    }

    fun tick() {
        this.flush()

        if (curTime - this.createdTimestamp >= HEARTBEAT_TIME) {

            // is Logging
            if (this.protocol is LoginProtocol
                && this.loginProgress != LoginProtocol.Progress.PRE_ACCEPT
                && this.loginProgress != LoginProtocol.Progress.ACCEPTED) {
                this.close(Component.text("Take to long to Login."))
                return
            }

        }
    }

    fun flush() {
        this
    }

    fun isOpen(): Boolean {
        return this.channel != null && this.channel!!.isActive
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        ctx ?: run { throw UnsupportedOperationException() }
        this.channel = ctx.channel()
        this.address = this.channel?.remoteAddress()
        this.protocol = Protocol.HANDSHAKE.protocol
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Packet?) {
        if (!this.isOpen())
            return

        msg!!.handle(this)
    }

    fun close(message: Component) {
        this.packetQueue.clear()
        this.channel?.let {
            if (it.isOpen) {
                it.close()
            }
        }
        this.disconnectMessage = message
    }

    data class PendingPacket(val packet: Packet, val listener: GenericFutureListener<*>?)

}