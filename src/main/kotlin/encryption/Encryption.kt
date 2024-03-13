package encryption

import fileio.storeKeyPair
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.util.io.Streams
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.KeyFlag
import org.pgpainless.algorithm.SymmetricKeyAlgorithm
import org.pgpainless.decryption_verification.ConsumerOptions
import org.pgpainless.decryption_verification.DecryptionStream
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.EncryptionStream
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.key.generation.KeySpec
import org.pgpainless.key.generation.type.ecc.EllipticCurve
import org.pgpainless.key.generation.type.ecc.ecdh.ECDH
import org.pgpainless.key.generation.type.ecc.ecdsa.ECDSA
import org.pgpainless.key.generation.type.rsa.RSA
import org.pgpainless.key.generation.type.rsa.RsaLength
import org.pgpainless.key.protection.SecretKeyRingProtector
import org.pgpainless.util.Passphrase.fromPassword
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun generateKeyPair(passphrase: String, name: String, email: String, length: RsaLength, vaultName: String) {
    val keyRing: PGPSecretKeyRing = PGPainless.buildKeyRing().setPrimaryKey(
        KeySpec.getBuilder(
            RSA.withLength(length), KeyFlag.SIGN_DATA, KeyFlag.CERTIFY_OTHER
        )
    ).addSubkey(
        KeySpec.getBuilder(ECDSA.fromCurve(EllipticCurve._P256), KeyFlag.SIGN_DATA)
    ).addSubkey(
        KeySpec.getBuilder(
            ECDH.fromCurve(EllipticCurve._P256), KeyFlag.ENCRYPT_COMMS, KeyFlag.ENCRYPT_STORAGE
        )
    ).addUserId("$name <$email>").setPassphrase(fromPassword(passphrase)).build()
    val fileName: String = vaultName
    val privateKey: ByteArray = keyRing.encoded
    storeKeyPair(privateKey, fileName)
}


fun encryptDirectory(directoryPath: String, privateKey: PGPSecretKeyRing, passphrase: String) {
    val directory = Paths.get(directoryPath).toAbsolutePath()
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()

    try {
        files.forEach { file ->
            val encryptedFilePath = Paths.get(directoryPath, "${file.name}.gpg").toAbsolutePath().toString()
            encryptFileStream(privateKey, file.inputStream(), File(encryptedFilePath).outputStream(), passphrase)
            file.run { delete() }
        }
    } catch (e: Exception) {
        println("Error encrypting files: ${e.message}")
    }

}

fun decryptDirectory(directoryPath: String, secretKey: PGPSecretKeyRing, passphrase: String) {
    val directory = Paths.get(directoryPath)
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()

    try {
        val secretKeyProtector = SecretKeyRingProtector.unlockAnyKeyWith(fromPassword(passphrase))

        files.forEach { file ->
            if (file.extension == ".gpg") {
                val decryptedFile = File(file.parent, file.name.removeSuffix(".gpg"))

                val options = ConsumerOptions().addDecryptionKey(secretKey, secretKeyProtector)

                val encryptedInputStream = FileInputStream(file)
                val decryptionStream: DecryptionStream =
                    PGPainless.decryptAndOrVerify().onInputStream(encryptedInputStream).withOptions(options)

                val outputStream = decryptedFile.outputStream()
                Streams.pipeAll(decryptionStream, outputStream)
                decryptionStream.close()
                outputStream.close()
            }

        }
    } catch (e: Exception) {
        println(e.printStackTrace())
    }

}

fun encryptFileStream(
    privateKey: PGPSecretKeyRing, inputStream: InputStream, outputStream: OutputStream, passphrase: String
) {
    try {
        SecretKeyRingProtector.unlockAnyKeyWith(fromPassword(passphrase))
        val publicKeyRing = PGPainless.extractCertificate(privateKey)
        val encryptionStream: EncryptionStream = PGPainless.encryptAndOrSign().onOutputStream(outputStream).withOptions(
            ProducerOptions.encrypt(
                EncryptionOptions().addRecipient(publicKeyRing)
                    .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_192)
                    .addPassphrase((fromPassword(passphrase)))
            ).setAsciiArmor(false)
        )

        // Pipe the input stream to the encryption stream
        inputStream.use { input ->
            encryptionStream.use { encryption ->
                input.copyTo(encryption)
            }
        }
    } catch (e: Exception) {
        println("Error encrypting file: ${e.message}")
    }
}

fun packageIntoArchive(sourceDir: Path, zipFilePath: Path, file: File?) {
    var result = ""
    try {
        val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath.toFile()))
        Files.walk(sourceDir).filter { Files.isRegularFile(it) }.forEach {
            val zipEntry = ZipEntry(sourceDir.relativize(it).toString())
            zipOutputStream.putNextEntry(zipEntry)
            if (it != null) {
                Files.copy(it, zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
        zipOutputStream.close()
        result = "Success!"
    } catch (e: Exception) {
        result = e.message.toString()
    }

    println(result)
}