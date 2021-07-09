package org.fairy.next.console

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.LogRecord

class ForwardLogHandler : ConsoleHandler() {

    private val cachedLoggers : MutableMap<String, Logger> = ConcurrentHashMap()

    private fun getLogger(name: String) : Logger {
        return cachedLoggers[name] ?: run {
            val loggerNew = LogManager.getLogger(name)
            cachedLoggers[name] = LogManager.getLogger(name)
            return loggerNew
        }
    }

    override fun publish(record: LogRecord?) {
        record?.let {
            val logger = getLogger(record.loggerName)
            val exception = record.thrown
            val message = formatter.formatMessage(record)

            when (level) {
                Level.SEVERE -> logger.error(message, exception)
                Level.WARNING -> logger.warn(message, exception)
                Level.INFO -> logger.info(message, exception)
                Level.CONFIG -> logger.debug(message, exception)
                else -> logger.trace(message, exception)
            }
        }
    }

    override fun flush() {

    }

    override fun close() {

    }

}