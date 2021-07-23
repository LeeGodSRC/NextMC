package org.fairy.next.server

import org.fairy.next.server.protocol.LoginProtocol
import org.fairy.next.server.protocol.PlayProtocol
import org.fairy.next.server.protocol.StatusProtocol
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.server.protocol.HandshakeProtocol

enum class Protocol(val protocol: AbstractProtocol) {

    HANDSHAKE(HandshakeProtocol()),
    PLAY(PlayProtocol()),
    STATUS(StatusProtocol()),
    LOGIN(LoginProtocol());

    init {
        this.protocol.init()
    }

    val id: Int
        get() = this.protocol.id

    companion object {
        fun findProtocol(id: Int): Protocol {
            values().forEach { if (it.protocol.id == id) return it }
            throw UnsupportedOperationException("Unknown Protocol ID $id")
        }
    }

}