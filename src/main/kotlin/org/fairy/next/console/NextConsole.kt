package org.fairy.next.console

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.fairy.next.NextMinecraft
import org.fairy.next.extension.logger
import org.fairy.next.thread.newThread
import java.util.concurrent.CountDownLatch

class NextConsole(val server : NextMinecraft) : SimpleTerminalConsole() {

    fun init() {
        val startLatch = CountDownLatch(1)
        newThread("Console Thread") {
            this@NextConsole.start()
        }

        val global = java.util.logging.Logger.getLogger("")
        global.useParentHandlers = false
        global.handlers.forEach { global.removeHandler(it) }
        global.addHandler(ForwardLogHandler())

        val logger = LogManager.getRootLogger()

        System.setOut(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream())
        System.setErr(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.WARN).buildPrintStream())

        startLatch.countDown()
        logger().info("Console Initialized!")
    }

    override fun isRunning(): Boolean {
        return this.server.isRunning()
    }

    override fun runCommand(command: String?) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

}