package org.fairy.next.server.protocol

import com.google.common.collect.ImmutableMap
import org.fairy.next.extension.isMutable
import org.fairy.next.extension.mutable
import org.fairy.next.org.fairy.next.server.packet.legacy.PacketLegacyHandshake
import org.fairy.next.org.fairy.next.server.packet.legacy.PacketLegacyPing
import org.fairy.next.org.fairy.next.server.packet.receive.PacketEncryptionResponse
import org.fairy.next.org.fairy.next.server.packet.send.PacketEncryptionRequest
import org.fairy.next.org.fairy.next.server.packet.receive.PacketPing
import org.fairy.next.org.fairy.next.server.packet.receive.PacketStatusStart
import org.fairy.next.org.fairy.next.server.packet.receive.PacketLoginStart
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.receive.PacketHandshake
import org.fairy.next.server.packet.Packet
import kotlin.reflect.KClass

abstract class AbstractProtocol(val id: Int) {

    private lateinit var receiveRegistry: Map<Int, KClass<out Packet>>
    private lateinit var sendRegistry: Map<Int, KClass<out Packet>>

    fun init() {
        this.receiveRegistry = HashMap()
        this.sendRegistry = HashMap()

        this.register()

        this.receiveRegistry = ImmutableMap.copyOf(this.receiveRegistry)
        this.sendRegistry = ImmutableMap.copyOf(this.sendRegistry)
    }

    fun registerReceive(id: Int, type: KClass<out Packet>) {
        if (!this.receiveRegistry.isMutable())
            throw IllegalArgumentException("You can't register any packets after post processed!")

        this.receiveRegistry.mutable()[id] = type
    }

    fun registerSend(id: Int, type: KClass<out Packet>) {
        if (!this.sendRegistry.isMutable())
            throw IllegalArgumentException("You can't register any packets after post processed!")

        this.sendRegistry.mutable()[id] = type
    }

    abstract fun register()

    open fun handleHandshake(networkHandler: NetworkHandler, packet: PacketHandshake) {
        throw UnsupportedOperationException()
    }

    open fun handleStatusStart(networkHandler: NetworkHandler, packet: PacketStatusStart) {
        throw UnsupportedOperationException()
    }

    open fun handlePing(networkHandler: NetworkHandler, packet: PacketPing) {
        throw UnsupportedOperationException()
    }

    open fun handleLoginStart(networkHandler: NetworkHandler, packet: PacketLoginStart) {
        throw UnsupportedOperationException()
    }

    open fun handleEncryptionResponse(networkHandler: NetworkHandler, packet: PacketEncryptionResponse) {
        throw UnsupportedOperationException()
    }

    open fun handleLegacyPing(networkHandler: NetworkHandler, packet: PacketLegacyPing) {
        throw UnsupportedOperationException()
    }

    open fun handleLegacyHandshake(networkHandler: NetworkHandler, packet: PacketLegacyHandshake) {
        throw UnsupportedOperationException()
    }

}