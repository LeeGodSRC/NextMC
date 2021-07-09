package org.fairy.next.server

import io.netty.channel.ChannelFactory
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.function.BiFunction

enum class TransportType(
    var socketName: String,
    var serverSocketChannelFactory: ChannelFactory<ServerSocketChannel>,
    var socketChannelFactory: ChannelFactory<SocketChannel>,
    var datagramChannelFactory: ChannelFactory<DatagramChannel>,
    var eventLoopGroupFactory: BiFunction<String, Type, EventLoopGroup>
    ) {

    NIO(
        "NIO",
        { NioServerSocketChannel() },
        { io.netty.channel.socket.nio.NioSocketChannel() },
        { io.netty.channel.socket.nio.NioDatagramChannel() },
        { name, type -> io.netty.channel.nio.NioEventLoopGroup(0, org.fairy.next.thread.threadFactory("Netty $name $type %d")) }
    ),
    EPOLL(
        "EPOLL",
        { EpollServerSocketChannel() },
        { io.netty.channel.epoll.EpollSocketChannel() },
        { io.netty.channel.epoll.EpollDatagramChannel() },
        { name, type -> io.netty.channel.epoll.EpollEventLoopGroup(0, org.fairy.next.thread.threadFactory("Netty $name $type %d")) }
    );

    fun createEventGroup(type: Type) : EventLoopGroup {
        return this.eventLoopGroupFactory.apply(socketName, type)
    }

    enum class Type(val typeName: String) {
        BOSS("Boss"), WORKER("Worker");

        override fun toString(): String {
            return typeName
        }
    }

    companion object {

        fun findType() : TransportType {
            return if (Epoll.isAvailable()) return EPOLL else NIO
        }

    }

}