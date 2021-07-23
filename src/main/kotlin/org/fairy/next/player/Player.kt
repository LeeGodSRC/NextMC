package org.fairy.next.player

import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import org.fairy.next.entity.Entity
import org.fairy.next.org.fairy.next.server.packet.send.PacketTeleport
import org.fairy.next.org.fairy.next.server.packet.send.TeleportFlag
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.packet.send.PacketDisconnect
import org.fairy.next.server.protocol.AbstractProtocol
import org.fairy.next.util.Location
import java.util.*

class Player(val networkHandler: NetworkHandler) : Entity() {

    val uuid: UUID
        get() = networkHandler.gameProfile.id

    val name: String
        get() = networkHandler.gameProfile.name

    val gameProfile: GameProfile
        get() = networkHandler.gameProfile

    var gamemode = Gamemode.SURVIVAL
    var targetPos: Location? = null
    var justTeleported: Boolean = false
    lateinit var protocol: AbstractProtocol

    fun teleport(location: Location, vararg flags: TeleportFlag) {
        if (location.world != this.location.world) {
            // TODO
            return
        }

        val target = location.copy()
        flags.forEach { it.add(target, this.location) }

        this.targetPos = target
        this.justTeleported = true

        this.location = target.copy()
        this.networkHandler.send(PacketTeleport(location, *flags))
    }

    fun disconnect(component: Component) {
        val packetDisconnect = PacketDisconnect()
        packetDisconnect.message = component
        this.networkHandler.send(packetDisconnect) {
            this.networkHandler.close(component)
        }

        this.networkHandler.disableRead()
    }

}