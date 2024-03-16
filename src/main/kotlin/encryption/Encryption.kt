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
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths


fun generateKeyPair(passphrase: String, vaultName: String) {

    val keyRing: PGPSecretKeyRing = PGPainless.buildKeyRing().setPrimaryKey(
        KeySpec.getBuilder(
            RSA.withLength(RsaLength._4096), KeyFlag.SIGN_DATA, KeyFlag.CERTIFY_OTHER
        )
    ).addSubkey(
        KeySpec.getBuilder(ECDSA.fromCurve(EllipticCurve._P256), KeyFlag.SIGN_DATA)
    ).addSubkey(
        KeySpec.getBuilder(
            ECDH.fromCurve(EllipticCurve._P256), KeyFlag.ENCRYPT_COMMS, KeyFlag.ENCRYPT_STORAGE
        )
    ).addUserId(vaultName).setPassphrase(fromPassword(passphrase)).build()

    val fileName: String = vaultName
    val privateKey: ByteArray = keyRing.encoded
    storeKeyPair(privateKey, fileName)
}


fun encryptDirectory(directoryPath: String, privateKey: PGPSecretKeyRing, passphrase: String) {
    val directory = Paths.get(directoryPath).toAbsolutePath()
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
    try {
        files.forEach { file ->
            if (file.isDirectory) {
                (encryptDirectory(file.absolutePath, privateKey, passphrase))
            }
            if (!file.name.endsWith(".gpg")) {
                val encryptedFilePath = file.toPath().resolveSibling("${file.name}.gpg").toAbsolutePath().toString()
                encryptFileStream(privateKey, file.inputStream(), File(encryptedFilePath).outputStream(), passphrase)
                file.delete()
            }

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

            if (file.name.contains(".gpg")) {
                val decryptedFile = File(file.parent, file.nameWithoutExtension)


                val options = ConsumerOptions().addDecryptionKey(secretKey, secretKeyProtector)

                val encryptedInputStream = FileInputStream(file)
                val decryptionStream: DecryptionStream =
                    PGPainless.decryptAndOrVerify().onInputStream(encryptedInputStream).withOptions(options)

                val outputStream = decryptedFile.outputStream()
                Streams.pipeAll(decryptionStream, outputStream)
                decryptionStream.close()
                outputStream.close()
                try {
                    with(file) {
                        delete()
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            if (file.isDirectory) {
                decryptDirectory(file.absolutePath, secretKey, passphrase)
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
}/*
fun createZipFile(files: List<File>, destinationPath: String) {
    val zipOutputStream = ZipOutputStream(FileOutputStream(destinationPath))

    for (file in files) {
        val zipEntry = ZipEntry(file.name)
        zipOutputStream.putNextEntry(zipEntry)

        val fileInputStream = file.inputStream()
        val buffer = ByteArray(1024)
        var length: Int

        while (fileInputStream.read(buffer).also { length = it } > 0) {
            zipOutputStream.write(buffer, 0, length)
        }

        fileInputStream.close()
        zipOutputStream.closeEntry()
    }

    zipOutputStream.close()
}
*/