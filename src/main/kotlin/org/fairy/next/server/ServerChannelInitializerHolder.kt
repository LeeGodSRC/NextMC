package org.fairy.next.server

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import org.fairy.next.extension.logger
import java.util.function.Supplier

class ServerChannelInitializerHolder(val initializerIn: ChannelInitializer<Channel>) : Supplier<ChannelInitializer<Channel>> {

    var initializer: ChannelInitializer<Channel> = initializerIn
        set(value) {
            logger().warn("The server channel initializer has been replaced by " + Thread.currentThread().stackTrace[2]);
            field = value
        }

    override fun get(): ChannelInitializer<Channel> {
        return this.initializer
    }

}