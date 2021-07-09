package org.fairy.next.extension

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

fun <T : Any> T.logger(): Logger = LogManager.getLogger(javaClass)

val log: Logger = LogManager.getLogger("NextMC")