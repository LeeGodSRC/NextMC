package org.fairy.next.player

import com.mojang.authlib.GameProfile
import org.fairy.next.org.fairy.next.entity.Entity
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.protocol.AbstractProtocol
import java.util.*

class Player(val networkHandler: NetworkHandler) : Entity() {

    val uuid: UUID
        get() = networkHandler.gameProfile.id

    val name: String
        get() = networkHandler.gameProfile.name

    val gameProfile: GameProfile
        get() = networkHandler.gameProfile

    lateinit var protocol: AbstractProtocol

}