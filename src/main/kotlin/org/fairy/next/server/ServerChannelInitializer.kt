package org.fairy.next.server

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import org.fairy.next.constant.FRAME_DECODER
import org.fairy.next.constant.LEGACY_PING_DECODER
import org.fairy.next.server.impl.LegacyPingDecoder

class ServerChannelInitializer : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel?) {
        ch?.pipeline()
            ?.addLast(LEGACY_PING_DECODER, LegacyPingDecoder())
//            ?.addLast(FRAME_DECODER, MinecraftVarIntDecoder())
//            ?.addLast(FRAME_DECODER, MinecraftVarIntDecoder())
    }
}