package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import javax.crypto.Cipher

class NettyEncryptingEncoder(cipher: Cipher?) : MessageToByteEncoder<ByteBuf?>() {
    private val encryptionCodec: NettyEncryptionTranslator = NettyEncryptionTranslator(cipher!!)

    override fun encode(p_encode_1_: ChannelHandlerContext, buf: ByteBuf?, out: ByteBuf) {
        encryptionCodec.cipher(buf!!, out)
    }

}