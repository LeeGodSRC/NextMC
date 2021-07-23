package org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.player.Gamemode
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.world.Difficulty
import org.fairy.next.server.writeString

class PacketJoinGame : SendPacket {

    var entityId = -1
    var hardcode = false
    var gamemode = Gamemode.NONE
    var dimension = -1
    var difficulty = Difficulty.EASY
    var maxPlayers = 60
    var worldType = "default"
    var reducedDebugInfo = false

    override fun encode(buf: ByteBuf) {
        buf.writeInt(this.entityId)
        buf.writeByte(this.gamemode.id.let { if (this.hardcode) it or 0x8 else it })
        buf.writeByte(this.dimension)
        buf.writeByte(this.difficulty.id)
        buf.writeByte(this.maxPlayers)
        buf.writeString(this.worldType)
        buf.writeBoolean(this.reducedDebugInfo)
    }

}