package org.fairy.next.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.EpollChannelOption
import io.netty.util.AttributeKey
import org.fairy.next.extension.logger
import org.fairy.next.org.fairy.next.server.protocol.LoginProtocol
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.server.ServerChannelInitializerHolder
import org.fairy.next.server.TransportType
import java.net.InetSocketAddress
import java.util.*


class NettyServer {

    companion object {
        val SERVER_WRITE_MARK = WriteBufferWaterMark(
            1 shl 20,
            1 shl 21
        )

        val LOCALE_ATTRIBUTE = AttributeKey.valueOf<Locale>("adventure:locale")
        val PROTOCOL_ATTRIBUTE = AttributeKey.valueOf<AbstractProtocol>("next:protocol")
    }

    lateinit var address: InetSocketAddress
    lateinit var channel: Channel

    lateinit var transportType: TransportType
    lateinit var bossGroup: EventLoopGroup
    lateinit var workerGroup: EventLoopGroup
    lateinit var serverChannelInitializerHolder: ServerChannelInitializerHolder

    fun boot() {
        this.transportType = TransportType.findType()
        this.bossGroup = this.transportType.createEventGroup(TransportType.Type.BOSS)
        this.workerGroup = this.transportType.createEventGroup(TransportType.Type.WORKER)
        this.serverChannelInitializerHolder = ServerChannelInitializerHolder(ServerChannelInitializer())

        this.bind(InetSocketAddress(25565))
    }

    fun bind(address: InetSocketAddress) {
        val serverBootstrap = ServerBootstrap()
            .channelFactory(this.transportType.serverSocketChannelFactory)
            .group(this.bossGroup, this.workerGroup)
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
            .childHandler(this.serverChannelInitializerHolder.get())
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.IP_TOS, 0x18)
            .localAddress(address)

        this.address = address

        serverBootstrap.childOption(EpollChannelOption.TCP_FASTOPEN, 3)
        serverBootstrap.bind()
            .addListener(ChannelFutureListener {
                val channel = it.channel()
                if (it.isSuccess) {
                    this.channel = channel
                    logger().info("Listening on {}", channel.localAddress())
                } else {
                    logger().error("Can't bind to {}", address, it.cause())
                }
            })
    }
}