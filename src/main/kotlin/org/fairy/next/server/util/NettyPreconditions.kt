package org.fairy.next.server.util

import com.google.common.base.Strings
import io.netty.handler.codec.CorruptedFrameException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

fun checkFrame(b: Boolean, message : String, vararg args : Any) {
    if (!b) {
        throw CorruptedFrameException(Strings.lenientFormat(message, args))
    }
}

/*
 * Create a new PublicKey from encoded X.509 data
 */
fun decodePublicKey(byteArray: ByteArray): PublicKey? {
    try {
        val var1 = X509EncodedKeySpec(byteArray)
        val var2 = KeyFactory.getInstance("RSA")
        return var2.generatePublic(var1)
    } catch (var3: NoSuchAlgorithmException) {
    } catch (var4: InvalidKeySpecException) {
    }
    System.err.println("Public key reconstitute failed!")
    return null
}