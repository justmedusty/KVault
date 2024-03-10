package encryption

import enums.Enums
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.SymmetricKeyAlgorithm
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.EncryptionStream
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.exception.MissingDecryptionMethodException
import org.pgpainless.key.generation.type.rsa.RsaLength
import org.pgpainless.util.Passphrase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun generateKeyPair(passphrase: String, name: String, email: String) {
    val keyRing: PGPSecretKeyRing = PGPainless.generateKeyRing().simpleRsaKeyRing("$name <$email>", RsaLength._4096)
    val fileName: String = name.trim() + "_" + SimpleDateFormat("yyyyMMdd").format(java.util.Date())
    storeKeyPair(keyRing, fileName)
}

fun storeKeyPair(keyRing: PGPSecretKeyRing, fileName: String) {

    val tempFile = File.createTempFile("temp_secret_key", ".asc")
    tempFile.deleteOnExit()

    val applicationFolder = File(System.getProperty("user.home"), Enums.APP_DIRECTORY.value)

    if (!applicationFolder.exists()) {
        applicationFolder.mkdirs()
    }

    val file = File(System.getProperty("user.home") + Enums.APP_DIRECTORY.value + "/$fileName" + ".asc")


    try {
        val inputStream = FileInputStream(tempFile)
        val outputStream = FileOutputStream(file)

        val buffer = ByteArray(inputStream.available())
        while (inputStream.read(buffer) != -1) {
            outputStream.write(buffer)
        }
        outputStream.close()
        inputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

fun encryptPrivateKey(keyRing: PGPSecretKeyRing, passphrase: String, outputFile: File): File {
    val tempFile = File.createTempFile("temp_secret_key", ".asc")
    tempFile.deleteOnExit()

    FileOutputStream(tempFile).use { outputStream ->
        keyRing.encode(outputStream)
    }

    return outputFile
}

fun encryptDirectory(directoryPath: String, publicKey: String, passphrase: String) {

    val directory = Paths.get(directoryPath)
    val files = Files.walk(directory).filter { Files.isRegularFile(it) }.map { it.toFile() }.toList()
    val tempDir = Files.createTempDirectory("encrypted_files")

    try {
        val publicKeyObj: PGPPublicKeyRing? = PGPainless.readKeyRing().publicKeyRing(publicKey)
        if (publicKeyObj != null) {

            files.forEach { file ->
                val encryptedFile = tempDir.resolve(file.name)
                encryptFile(file, encryptedFile.toFile(), publicKeyObj, passphrase)
                packageIntoArchive(tempDir, Paths.get("encrypted_files.zip"))
            }
        }


    } finally {
        // Cleanup temporary directory
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }
}


fun encryptFile(inputFile: File, outputFile: File, publicKey: PGPPublicKeyRing, passphrase: String) {

    try {
        val passphraseObj: Passphrase = Passphrase.fromPassword(passphrase)
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

    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: PGPException) {
        e.printStackTrace()
    } catch (e: MissingDecryptionMethodException) {
        e.printStackTrace()
    }
}

fun packageIntoArchive(sourceDir: java.nio.file.Path, zipFilePath: java.nio.file.Path) {
    val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath.toFile()))
    Files.walk(sourceDir).filter { Files.isRegularFile(it) }.forEach { file ->
        val zipEntry = ZipEntry(sourceDir.relativize(file).toString())
        zipOutputStream.putNextEntry(zipEntry)
        Files.copy(file, zipOutputStream)
        zipOutputStream.closeEntry()
    }
    zipOutputStream.close()
}