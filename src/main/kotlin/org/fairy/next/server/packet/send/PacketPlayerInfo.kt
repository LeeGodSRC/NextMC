package org.fairy.next.server.packet.send

import com.mojang.authlib.GameProfile
import io.netty.buffer.ByteBuf
import net.kyori.adventure.text.Component
import org.fairy.next.player.Gamemode
import org.fairy.next.player.Player
import org.fairy.next.server.*
import org.fairy.next.server.packet.SendPacket

class PacketPlayerInfo(var action: PlayerInfoAction, vararg var infoDataList: PlayerInfoData): SendPacket {

    override fun encode(buf: ByteBuf) {
        buf
            .writeEnum(action)
            .writeCollection(infoDataList) {
                buf.writeUuid(it.gameProfile.id)
                when (action) {
                    PlayerInfoAction.ADD_PLAYER -> {
                        buf.writeString(it.gameProfile.name)
                        buf.writeVarInt(it.gameProfile.properties.size())
                        it.gameProfile.properties.values().forEach { property ->
                            buf.writeString(property.name)
                            buf.writeString(property.value)
                            if (property.hasSignature()) {
                                buf.writeBoolean(true)
                                buf.writeString(property.signature)
                            } else {
                                buf.writeBoolean(false)
                            }
                        }

                        buf.writeVarInt(it.gamemode.id)
                        buf.writeVarInt(it.latency)
                        it.component?.let { comp ->
                            buf.writeBoolean(true)
                            buf.writeComponent(comp)
                        } ?: buf.writeBoolean(false)
                    }
                    PlayerInfoAction.UPDATE_GAME_MODE -> {
                        buf.writeVarInt(it.gamemode.id)
                    }
                    PlayerInfoAction.UPDATE_LATENCY -> {
                        buf.writeVarInt(it.latency)
                    }
                    PlayerInfoAction.UPDATE_DISPLAY_NAME -> {
                        it.component?.let { comp ->
                            buf.writeBoolean(true)
                            buf.writeComponent(comp)
                        } ?: buf.writeBoolean(false)
                    }
                }
            }
    }

}

enum class PlayerInfoAction {

    ADD_PLAYER,
    UPDATE_GAME_MODE,
    UPDATE_LATENCY,
    UPDATE_DISPLAY_NAME,
    REMOVE_PLAYER

}

data class PlayerInfoData(val latency: Int, val gamemode: Gamemode, val gameProfile: GameProfile, val component: Component?) {
    constructor(player: Player): this(player.networkHandler.ping.toInt(), player.gamemode, player.gameProfile, null)
}