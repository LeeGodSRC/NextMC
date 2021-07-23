package org.fairy.next.server

import com.mojang.authlib.GameProfile
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.local.LocalChannel
import io.netty.channel.local.LocalServerChannel
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import net.kyori.adventure.text.Component
import org.fairy.next.constant.*
import org.fairy.next.extension.curTime
import org.fairy.next.extension.log
import org.fairy.next.org.fairy.next.server.impl.NettyEncryptingDecoder
import org.fairy.next.org.fairy.next.server.impl.NettyEncryptingEncoder
import org.fairy.next.server.impl.CompressDecoder
import org.fairy.next.server.impl.CompressEncoder
import org.fairy.next.server.packet.both.PacketKeepAlive
import org.fairy.next.server.protocol.LoginProtocol
import org.fairy.next.player.Player
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.server.packet.Packet
import org.fairy.next.util.createNetCipherInstance
import java.lang.Exception
import java.net.SocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.crypto.SecretKey
import kotlin.concurrent.write

class NetworkHandler : SimpleChannelInboundHandler<Packet>() {

    private val packetQueue = ConcurrentLinkedQueue<PendingPacket>()
    private val lock = ReentrantReadWriteLock()
    var statusPerformed = false

    lateinit var gameProfile: GameProfile

    lateinit var player: Player
    var protocol: AbstractProtocol? = null
        set(value) {
            this.channel?.let {
                if (field != null) {
                    it.config().isAutoRead = false
                    this.flush().join()
                }

                it.attr(NettyServer.PROTOCOL_ATTRIBUTE).set(value)
                it.config().setAutoRead(true)
            }
            field = value
        }

    private var channel: Channel? = null
    private var disconnectMessage: Component? = null
    var address: SocketAddress? = null
    var version = -1
    var host: String? = null
    var keepAliveId: Long = -1
    var keepAliveTime: Long = -1
    var ping: Long = 0

    var handledDisconnect = false
    var preparing = true

    val createdTimestamp = curTime

    var loginProgress: LoginProtocol.Progress = LoginProtocol.Progress.KEY
    var loginToken: ByteArray? = null
    var loginKey: SecretKey? = null
    set(value) {
        field?.run { throw IllegalStateException("Already registered encryption!") }
        value?.let {
            this.channel?.pipeline()?.addBefore(FRAME_DECODER, CIPHER_DECODER, NettyEncryptingDecoder(createNetCipherInstance(2, it)))
            this.channel?.pipeline()?.addBefore(FRAME_ENCODER, CIPHER_ENCODER, NettyEncryptingEncoder(createNetCipherInstance(1, it)))
        }
        field = value
    }

    fun send(packet: Packet) = this.lock.write {
        this.packetQueue += PendingPacket(packet, null)
    }

    fun send(packet: Packet, listener: GenericFutureListener<out Future<in Void>>?) = this.lock.write {
        this.packetQueue += PendingPacket(packet, listener)
    }

    fun disableRead() = this.channel?.config()!!.setAutoRead(false)

    fun isLocalChannel(): Boolean = this.channel is LocalChannel || this.channel is LocalServerChannel

    fun setupCompression(threshold: Int) {
        val pipeline = this.channel!!.pipeline()!!

        if (threshold >= 0) {
            val originalDecompress = pipeline.get(COMPRESSION_DECODER)
            if (originalDecompress is CompressDecoder) {
                originalDecompress.threshold = threshold
            } else {
                pipeline.addBefore(MINECRAFT_DECODER, COMPRESSION_DECODER, CompressDecoder(threshold))
            }

            val originalCompress = pipeline.get(COMPRESSION_ENCODER)
            if (originalCompress is CompressEncoder) {
                originalCompress.threshold = threshold
            } else {
                pipeline.addBefore(MINECRAFT_ENCODER, COMPRESSION_ENCODER, CompressEncoder(threshold))
            }
        } else {
            if (pipeline.get(COMPRESSION_DECODER) is CompressDecoder) {
                pipeline.remove(COMPRESSION_DECODER)
            }

            if (pipeline.get(COMPRESSION_ENCODER) is CompressEncoder) {
                pipeline.remove(COMPRESSION_ENCODER)
            }
        }
    }

    fun tick() {
        this.flush()

        val curTime = curTime
        if (curTime - this.createdTimestamp >= HEARTBEAT_TIME) {
            // is Logging
            if (this.protocol is LoginProtocol
                && this.loginProgress != LoginProtocol.Progress.PRE_ACCEPT
                && this.loginProgress != LoginProtocol.Progress.ACCEPTED) {
                this.close(Component.text("Take to long to Login."))
                return
            }
        }

        if (this.protocol == Protocol.PLAY.protocol && curTime - this.keepAliveTime >= HEARTBEAT_TIME) {
            this.keepAliveTime = curTime
            this.keepAliveId = this.keepAliveTime

            val packet = PacketKeepAlive()
            packet.id = this.keepAliveId

            this.send(packet)
        }
    }

    fun flush(): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        val f = {
            var packet: PendingPacket? = null
            while (this.packetQueue.poll()?.let { packet = it; true } == true) {
                val channelFuture = this.channel!!.write(packet!!.packet)

                packet!!.listener?.let { channelFuture.addListener(it) }
                channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
            }

            channel?.flush()
            future.complete(null)
        }

        if (this.channel!!.eventLoop().inEventLoop()) {
            f.invoke()
        } else {
            this.channel!!.eventLoop().submit(f)
        }
        return future
    }

    fun isOpen(): Boolean {
        return this.channel != null && this.channel!!.isOpen
    }

    fun hasChannel(): Boolean = this.channel != null

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        ctx ?: run { throw UnsupportedOperationException() }
        this.preparing = false
        this.channel = ctx.channel()
        this.address = this.channel?.remoteAddress()
        this.protocol = Protocol.HANDSHAKE.protocol
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Packet?) {
        if (!this.channel!!.isOpen)
            return

        try {
            msg!!.handle(this)
        } catch (e: Exception) {
            log.error("An error occurs while handling packet " + this.protocol?.let { it::class.simpleName }, e)
        }
    }

    fun close(message: Component) {
        this.flush()
        this.preparing = false
        this.channel?.let {
            if (it.isOpen) {
                it.close()
            }
        }
        this.disconnectMessage = message
    }

    fun handleDisconnection() {
        this.channel?. let {
            if (!it.isOpen) {
                if (!handledDisconnect) {
                    this.handledDisconnect = true
                } else {
                    log.warn("handleDisconnection() called twice")
                }
            }
        }
    }

    data class PendingPacket(val packet: Packet, val listener: GenericFutureListener<out Future<in Void>>?)

}