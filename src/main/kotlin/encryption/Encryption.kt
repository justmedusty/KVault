package encryption

import fileio.storeKeyPair
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPPublicKeyRing
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
import org.pgpainless.encryption_signing.SigningOptions
import org.pgpainless.exception.MissingDecryptionMethodException
import org.pgpainless.key.generation.KeySpec
import org.pgpainless.key.generation.type.ecc.EllipticCurve
import org.pgpainless.key.generation.type.ecc.ecdh.ECDH
import org.pgpainless.key.generation.type.ecc.ecdsa.ECDSA
import org.pgpainless.key.generation.type.rsa.RSA
import org.pgpainless.key.generation.type.rsa.RsaLength
import org.pgpainless.key.protection.SecretKeyRingProtector
import org.pgpainless.util.Passphrase
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


fun encryptDirectory(directoryPath: String, publicKey: String, passphrase: String, vaultName: String) {
    val directory = Paths.get(directoryPath).toAbsolutePath()
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()

    try {
        files.forEach { file ->
            val encryptedFilePath = Paths.get(directoryPath, "${file.name}.gpg").toAbsolutePath().toString()
            encryptFile(file, File(encryptedFilePath), publicKey, passphrase)
            packageIntoArchive(directory, directory, file)
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
            val decryptedFile = File(file.parent, "${file.nameWithoutExtension}_decrypted.${file.extension}")

            val options = ConsumerOptions().addDecryptionKey(secretKey, secretKeyProtector)

            val encryptedInputStream = FileInputStream(file)
            val decryptionStream: DecryptionStream =
                PGPainless.decryptAndOrVerify().onInputStream(encryptedInputStream).withOptions(options)

            val outputStream = decryptedFile.outputStream()
            Streams.pipeAll(decryptionStream, outputStream)
            decryptionStream.close()
            outputStream.close()
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
        val publicKey = PGPPublicKeyRing.insertPublicKey(publicKeyRing, privateKey.publicKey)
        val encryptionOptions = EncryptionOptions().addRecipient(publicKey).addPassphrase(fromPassword(passphrase))

        val producerOptions = ProducerOptions.encrypt(encryptionOptions)

        val encryptionStream: EncryptionStream =
            PGPainless.encryptAndOrSign().onOutputStream(outputStream).withOptions(producerOptions)

        Streams.pipeAll(inputStream, outputStream)
        encryptionStream.close()


    } catch (e: Exception) {
        println("Error encrypting file: ${e.message}")
    } finally {
        inputStream.close()
        outputStream.close()
    }
}

fun encryptFile(inputFile: File, outputFile: File, publicKey: String, passphrase: String): String {
    var result = ""
    try {

        if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw Exception()
        }
        val publicKeyObj: PGPPublicKeyRing = PGPainless.readKeyRing().publicKeyRing(publicKey) ?: throw Exception()

        val passphraseObj: Passphrase = fromPassword(passphrase)

        val encryptionOptions: EncryptionOptions =
            EncryptionOptions().addRecipient(publicKeyObj).overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_192)
        val encryptedContent = PGPainless.encryptAndOrSign().onOutputStream(outputFile.outputStream()).withOptions(
            ProducerOptions.signAndEncrypt(encryptionOptions, SigningOptions())
        )

        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        encryptedContent.use { encryptionStream ->
            inputFile.inputStream().use { inputStream ->
                if (encryptionStream != null) {
                    inputStream.copyTo(encryptionStream)
                    result = "Success!"
                }
            }
        }


    } catch (e: IOException) {
        result = e.localizedMessage
    } catch (e: PGPException) {
        result = e.message.toString()
    } catch (e: MissingDecryptionMethodException) {
        result = e.message.toString()
    } catch (e: Exception) {
        result = e.message.toString()
    }

    return result
}

fun packageIntoArchive(sourceDir: Path, zipFilePath: Path, file: File?) {
    var result = ""
    try {
        val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath.toFile()))
        Files.walk(sourceDir).filter { Files.isRegularFile(it) }.forEach { file ->
            val zipEntry = ZipEntry(sourceDir.relativize(file).toString())
            zipOutputStream.putNextEntry(zipEntry)
            if (file != null) {
                Files.copy(file, zipOutputStream)
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