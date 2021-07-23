package org.fairy.next.org.fairy.next.server.packet.send

import io.netty.buffer.ByteBuf
import org.fairy.next.server.packet.SendPacket
import org.fairy.next.util.Location

class PacketTeleport(private val location: Location, private vararg val flags: TeleportFlag): SendPacket {

    override fun encode(buf: ByteBuf) {
        buf.writeDouble(location.x)
        buf.writeDouble(location.y)
        buf.writeDouble(location.z)
        buf.writeFloat(location.yaw)
        buf.writeFloat(location.pitch)
        buf.writeByte(flagToInt(flags))
    }

}

enum class TeleportFlag(val id: Int) {

    X(0) {
        override fun add(to: Location, from: Location) {
            to.x += from.x
        }
    },
    Y(1) {
        override fun add(to: Location, from: Location) {
            to.y += from.y
        }
    },
    Z(2) {
        override fun add(to: Location, from: Location) {
            to.z += from.z
        }
    },
    YAW(3) {
        override fun add(to: Location, from: Location) {
            to.yaw += from.yaw
        }
    },
    PITCH(4) {
        override fun add(to: Location, from: Location) {
            to.pitch += from.pitch
        }
    };

    abstract fun add(to: Location, from: Location)

}

private fun flagToInt(flags: Array<out TeleportFlag>): Int {
    var i = 0
    flags.forEach {
        i = i or it.id
    }

    return i
}
