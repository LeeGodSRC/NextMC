package org.fairy.next.server

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.fairy.next.constant.*
import org.fairy.next.extension.mc
import org.fairy.next.org.fairy.next.server.impl.MinecraftDecoder
import org.fairy.next.org.fairy.next.server.impl.MinecraftEncoder
import org.fairy.next.org.fairy.next.server.impl.MinecraftVarintFrameDecoder
import org.fairy.next.org.fairy.next.server.impl.MinecraftVarintLengthEncoder
import org.fairy.next.server.impl.LegacyPingDecoder

class ServerChannelInitializer : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel?) {
        ch?.config()?.setOption(ChannelOption.TCP_NODELAY, true)
        val networkHandler = NetworkHandler()

        ch?.pipeline()
            ?.addLast(READ_TIMEOUT, ReadTimeoutHandler(30))
            ?.addLast(LEGACY_PING_DECODER, LegacyPingDecoder())
            ?.addLast(FRAME_DECODER, MinecraftVarintFrameDecoder())
            ?.addLast(MINECRAFT_DECODER, MinecraftDecoder(networkHandler))
            ?.addLast(FRAME_ENCODER, MinecraftVarintLengthEncoder.INSTANCE)
            ?.addLast(MINECRAFT_ENCODER, MinecraftEncoder(networkHandler))
            ?.addLast(HANDLER, networkHandler)

        mc.nettyServer.pendingConnections += networkHandler
    }
}