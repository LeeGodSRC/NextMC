package org.fairy.next.constant

const val CIPHER_DECODER = "cipher-decoder"
const val CIPHER_ENCODER = "cipher-encoder"
const val COMPRESSION_DECODER = "compression-decoder"
const val COMPRESSION_ENCODER = "compression-encoder"
const val FLOW_HANDLER = "flow-handler"
const val FRAME_DECODER = "frame-decoder"
const val FRAME_ENCODER = "frame-encoder"
const val HANDLER = "handler"
const val LEGACY_PING_DECODER = "legacy-ping-decoder"
const val LEGACY_PING_ENCODER = "legacy-ping-encoder"
const val MINECRAFT_DECODER = "minecraft-decoder"
const val MINECRAFT_ENCODER = "minecraft-encoder"
const val READ_TIMEOUT = "read-timeout"

const val DEFAULT_MAX_STRING_SIZE = 65536 // 64KiB

/**
 * Clients attempting to connect to 1.8-1.12.2 Forge servers will have
 * this token appended to the hostname in the initial handshake
 * packet.
 */
const val HANDSHAKE_HOSTNAME_TOKEN = "\u0000FML\u0000"

// This size was chosen to ensure Forge clients can still connect even with very long hostnames.
// While DNS technically allows any character to be used, in practice ASCII is used.
const val MAXIMUM_HOSTNAME_LENGTH: Int = 255 + HANDSHAKE_HOSTNAME_TOKEN.length + 1

const val MAXIMUM_UNCOMPRESSED_SIZE = 8 * 1024 * 1024; // 8MiB

const val HEARTBEAT_TIME = 30L * 1000L