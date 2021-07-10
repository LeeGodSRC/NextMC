package org.fairy.next.player

import org.fairy.next.org.fairy.next.util.Location
import org.fairy.next.server.NetworkHandler
import org.fairy.next.server.Protocol
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.read
import kotlin.concurrent.write

class PlayerStorage {

    private val players: MutableList<Player> = ArrayList()
    private val playerUuids: MutableMap<UUID, Player> = HashMap()
    private val playerNames: MutableMap<String, Player> = HashMap()
    private val lock = ReentrantReadWriteLock()

    fun forEach(f: (Player) -> Unit) = this.lock.read {
        this.players.forEach(f)
    }

    fun get(uuid: UUID) : Player? = this.lock.read {
        return this.playerUuids[uuid]
    }

    fun get(name: String) : Player? = this.lock.read {
        return this.playerNames[name]
    }

    fun add(player: Player) = this.lock.write {
        this.playerUuids[player.uuid]?.run { throw IllegalArgumentException() }

        this.playerUuids[player.uuid] = player
        this.playerNames[player.name] = player
        this.players += player
    }

    fun remove(player: Player) : Boolean = this.lock.write {
        var deletedAny = false

        playerUuids.remove(player.uuid)?.let { deletedAny = true }
        playerNames.remove(player.name)?.let { deletedAny = true }
        if (players.remove(player)) deletedAny = true

        return deletedAny
    }

    fun processLogin(networkHandler: NetworkHandler, player: Player) {
        // TODO load player data

        networkHandler.protocol = Protocol.PLAY.protocol
        val location = Location("world", 0, 0, 0)


    }

}