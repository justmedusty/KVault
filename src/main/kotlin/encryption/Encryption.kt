package encryption

import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.CompressionAlgorithm
import org.pgpainless.algorithm.KeyFlag
import org.pgpainless.algorithm.SymmetricKeyAlgorithm
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
import org.pgpainless.util.Passphrase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
fun generateKeyPair(passphrase: String,name:String, email : String) {
    val keyRing: PGPSecretKeyRing = PGPainless.buildKeyRing()
        .setPrimaryKey(
            KeySpec.getBuilder(
                RSA.withLength(RsaLength._8192),
                KeyFlag.SIGN_DATA, KeyFlag.CERTIFY_OTHER
            )
        )
        .addSubkey(
            KeySpec.getBuilder(ECDSA.fromCurve(EllipticCurve._P256), KeyFlag.SIGN_DATA)
                .overridePreferredCompressionAlgorithms(CompressionAlgorithm.ZLIB)
        ).addSubkey(
            KeySpec.getBuilder(
                ECDH.fromCurve(EllipticCurve._P256),
                KeyFlag.ENCRYPT_COMMS, KeyFlag.ENCRYPT_STORAGE
            )
        ).addUserId(name)
        .addUserId(email)
        .setPassphrase(Passphrase.fromPassword(passphrase))
        .build()
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
        val encryptionStream: EncryptionStream =
            PGPainless.encryptAndOrSign().onOutputStream(outputFile.outputStream()).withOptions(
                ProducerOptions.encrypt(
                    EncryptionOptions().addRecipient(publicKey)
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