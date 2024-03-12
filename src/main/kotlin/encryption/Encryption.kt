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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
    val privateKey: ByteArray =keyRing.encoded
    storeKeyPair(privateKey, fileName)
}


fun encryptDirectory(directoryPath: String, publicKey: String, passphrase: String) {

    val directory = Paths.get(directoryPath)
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
    val tempDir = Files.createTempDirectory("encrypted_files")

    try {
        val publicKeyObj: PGPPublicKeyRing? = PGPainless.readKeyRing().publicKeyRing(publicKey)
        if (publicKeyObj != null) {

            files.forEach { file ->
                val encryptedFile = tempDir.resolve("${file.name}.gpg")
                encryptFile(file, encryptedFile.toFile(), publicKeyObj, passphrase)
                packageIntoArchive(tempDir, Paths.get("encrypted_files.zip"))
            }
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

fun encryptFile(inputFile: File, outputFile: File, publicKey: PGPPublicKeyRing, passphrase: String): String {

    try {
        val passphraseObj: Passphrase = fromPassword(passphrase)
        val encryptionStream: EncryptionStream =
            PGPainless.encryptAndOrSign().onOutputStream(outputFile.outputStream()).withOptions(
                ProducerOptions.encrypt(
                    EncryptionOptions().addRecipient(publicKey).addPassphrase(passphraseObj)
                        .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_192)
                ).setAsciiArmor(false)
            )
        val inputStream = inputFile.inputStream()
        // Pipe the input stream to the encryption stream
        inputStream.use { input ->
            encryptionStream.use { encryption ->
                input.copyTo(encryption)
            }
        }
        return "Success!"

    } catch (e: IOException) {
        return "IO Exception Occurred"
    } catch (e: PGPException) {
        return "PGP Exception Occurred"
    } catch (e: MissingDecryptionMethodException) {
        return "MissingDecryptionMethod Exception Occurred"
    }
}

fun packageIntoArchive(sourceDir: Path, zipFilePath: Path) {
    val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath.toFile()))
    Files.walk(sourceDir).filter { Files.isRegularFile(it) }.forEach { file ->
        val zipEntry = ZipEntry(sourceDir.relativize(file).toString())
        zipOutputStream.putNextEntry(zipEntry)
        Files.copy(file, zipOutputStream)
        zipOutputStream.closeEntry()
    }
    zipOutputStream.close()
}