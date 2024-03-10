package fileio

import enums.Enums
import org.bouncycastle.openpgp.PGPSecretKey
import org.pgpainless.key.protection.UnlockSecretKey
import java.io.*
import java.nio.file.Files

fun storeKeyPair(privateKey: ByteArray, fileName: String) {

    val tempFile = File.createTempFile("temp_secret_key", ".asc")
    Files.write(tempFile.toPath(), privateKey)
    tempFile.deleteOnExit()

    val applicationFolder = File(System.getProperty(Enums.HOME_DIR.value), Enums.APP_DIRECTORY.value)

    if (!applicationFolder.exists()) {
        applicationFolder.mkdirs()
    }

    val file = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + "/$fileName")


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

fun retrieveKeyPair(fileName: String): PGPSecretKey? {
    val file: File = File(System.getProperty(Enums.HOME_DIR.value), Enums.APP_DIRECTORY.value + "/$fileName")
    if (file.exists()) {
        val privateKey: ByteArray = Files.readAllBytes(file.toPath())
        val inputStream: InputStream = ByteArrayInputStream(privateKey)
    }
}