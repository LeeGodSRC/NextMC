package org.fairy.next.org.fairy.next.server.impl

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import javax.crypto.Cipher
import javax.crypto.ShortBufferException

class NettyEncryptingDecoder(cipher: Cipher?) : MessageToMessageDecoder<ByteBuf?>() {
    private val decryptionCodec: NettyEncryptionTranslator = NettyEncryptionTranslator(cipher!!)

    @Throws(ShortBufferException::class, Exception::class)
    override fun decode(p_decode_1_: ChannelHandlerContext, p_decode_2_: ByteBuf?, p_decode_3_: MutableList<Any>) {
        p_decode_3_.add(decryptionCodec.decipher(p_decode_1_, p_decode_2_!!))
    }

}