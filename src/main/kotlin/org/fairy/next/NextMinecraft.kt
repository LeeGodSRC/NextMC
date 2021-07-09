package org.fairy.next

import com.mojang.authlib.GameProfileRepository
import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository
import org.fairy.next.console.NextConsole
import org.fairy.next.UpdateScheduler
import org.fairy.next.org.fairy.next.util.createNewKeyPair
import org.fairy.next.player.PlayerStorage
import org.fairy.next.world.WorldContainer
import org.fairy.next.server.NettyServer
import org.fairy.next.thread.curThread
import java.net.Proxy
import java.security.KeyPair
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class NextMinecraft {

    companion object {

        val INSTANCE = NextMinecraft()

    }

    val networkCompressionThreshold: Int
        get() = 256

    lateinit var updateScheduler: UpdateScheduler
    lateinit var playerStorage: PlayerStorage
    lateinit var nettyServer: NettyServer
    lateinit var worldContainer: WorldContainer
    lateinit var keyPair: KeyPair

    lateinit var yggdrasilAuthenticationService: YggdrasilAuthenticationService
    lateinit var minecraftSessionService: MinecraftSessionService
    lateinit var profileRepository: GameProfileRepository

    val onlineMode = true

    private val running = AtomicBoolean(true);

    fun isRunning() : Boolean {
        return this.running.get();
    }

    fun launch() {
        val console = NextConsole(this)
        console.init()

        this.nettyServer = NettyServer()
        this.nettyServer.boot()

        this.playerStorage = PlayerStorage()
        this.worldContainer = WorldContainer()

        this.keyPair = createNewKeyPair()!!
        this.yggdrasilAuthenticationService = YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString())
        this.minecraftSessionService = this.yggdrasilAuthenticationService.createMinecraftSessionService()
        this.profileRepository = this.yggdrasilAuthenticationService.createProfileRepository()

        this.updateScheduler = UpdateScheduler(this)
        this.updateScheduler.start()
    }

    fun isMainThread(): Boolean {
        return this.updateScheduler.thread == curThread
    }

}