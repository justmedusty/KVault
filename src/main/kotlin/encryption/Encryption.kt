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


fun encryptDirectory(directoryPath: String, publicKey: String, passphrase: String) {

    val directory = Paths.get(directoryPath)
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
    val tempDir = Files.createTempDirectory("encrypted_files")

    try {

        files.forEach { file ->
            val encryptedFile = tempDir.resolve("${file.name}.gpg")
            encryptFile(file, encryptedFile.toFile(), publicKey, passphrase)
            packageIntoArchive(tempDir, Paths.get("encrypted_files.zip"))
        }


    } finally {
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
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

fun encryptFile(inputFile: File, outputFile: File, publicKey: String, passphrase: String): String {

    try {
        // Read the public key from the string
        val publicKeyObj: PGPPublicKeyRing = PGPainless.readKeyRing().publicKeyRing(publicKey) ?: return "Invalid public key"

        // Convert passphrase to Passphrase object
        val passphraseObj: Passphrase = fromPassword(passphrase)

        // Prepare encryption options
        val encryptionOptions: EncryptionOptions = EncryptionOptions()
            .addRecipient(publicKeyObj)
            .addPassphrase(passphraseObj)
            .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_192)
        val encryptedContent = PGPainless.encryptAndOrSign().onOutputStream(outputFile.outputStream()).withOptions(
            ProducerOptions.signAndEncrypt(encryptionOptions,null))

        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }

        // Encrypt the content
        encryptedContent.use { encryptionStream ->
            inputFile.inputStream().use { inputStream ->
                if (encryptionStream != null) {
                    inputStream.copyTo(encryptionStream)
                }
            }
        }

        return "Success!"

    } catch (e: IOException) {
        return e.localizedMessage
    } catch (e: PGPException) {
        return e.message.toString()
    } catch (e: MissingDecryptionMethodException) {
        return e.message.toString()
    } catch (e: Exception) {
        return e.message.toString()
    }
}

fun packageIntoArchive(sourceDir: Path, zipFilePath: Path) {
    try {
        val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath.toFile()))
        Files.walk(sourceDir).filter { Files.isRegularFile(it) }.forEach { file ->
            val zipEntry = ZipEntry(sourceDir.relativize(file).toString())
            zipOutputStream.putNextEntry(zipEntry)
            Files.copy(file, zipOutputStream)
            zipOutputStream.closeEntry()
        }
        zipOutputStream.close()
    } catch (e: Exception) {
        println(e.message.toString())
    }

}