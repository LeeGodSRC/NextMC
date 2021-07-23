package org.fairy.next.util

import org.fairy.next.extension.log
import java.io.UnsupportedEncodingException
import java.security.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Compute a serverId hash for use by sendSessionRequest()
 */
fun getServerIdHash(serverId: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray? {
    return try {
        digestOperation("SHA-1", serverId.toByteArray(charset("ISO_8859_1")), secretKey.encoded, publicKey.encoded)
    } catch (ex: UnsupportedEncodingException) {
        ex.printStackTrace()
        null
    }
}

/**
 * Compute a message digest on arbitrary byte[] data
 */
fun digestOperation(algorithm: String, vararg data: ByteArray): ByteArray? {
    return try {
        val messageDigest = MessageDigest.getInstance(algorithm)
        for (byte in data) {
            messageDigest.update(byte)
        }
        messageDigest.digest()
    } catch (ex: NoSuchAlgorithmException) {
        ex.printStackTrace()
        null
    }
}

fun createNewKeyPair(): KeyPair? = try {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(1024)
    keyPairGenerator.generateKeyPair()
} catch (ex: NoSuchAlgorithmException) {
    ex.printStackTrace()
    System.err.println("Key pair generation failed!")
    null
}

fun createNetCipherInstance(opMode: Int, key: Key): Cipher? {
    return try {
        val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        cipher.init(opMode, key, IvParameterSpec(key.encoded))
        cipher
    } catch (ex: GeneralSecurityException) {
        throw RuntimeException(ex)
    }
}

fun decryptToSecretKey(privateKey: PrivateKey, byteArray: ByteArray) = SecretKeySpec(decryptData(privateKey, byteArray), "AES")

fun decryptData(key: Key, byteArray: ByteArray): ByteArray? = cipherOperation(2, key, byteArray)

fun cipherOperation(opMode: Int, key: Key, data: ByteArray): ByteArray? {
    try {
        return createTheCipherInstance(opMode, key.algorithm, key)!!.doFinal(data)
    } catch (ex: IllegalBlockSizeException) {
        ex.printStackTrace()
    } catch (ex: BadPaddingException) {
        ex.printStackTrace()
    }
    log.error("Cipher data failed!")
    return null
}

/**
 * Creates the Cipher Instance.
 */
fun createTheCipherInstance(opMode: Int, transformation: String, key: Key): Cipher? {
    try {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(opMode, key)
        return cipher
    } catch (ex: InvalidKeyException) {
        ex.printStackTrace()
    } catch (ex: NoSuchAlgorithmException) {
        ex.printStackTrace()
    } catch (ex: NoSuchPaddingException) {
        ex.printStackTrace()
    }
    log.error("Cipher creation failed!")
    return null
}