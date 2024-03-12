package fileio

import enums.Enums
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.PGPainless
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files

fun storeKeyPair(privateKey: String, vaultName: String) {

    val tempFile = File.createTempFile("temp_secret_key", ".asc")
    Files.write(tempFile.toPath(), privateKey.toByteArray())

    val applicationFolder =
        File(System.getProperty(Enums.HOME_DIR.value), Enums.APP_DIRECTORY.value + "/${Enums.KEY_DIR.value}")

    if (!applicationFolder.exists()) {
        applicationFolder.mkdirs()
    }

    val file = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + "/$vaultName.asc")


    try {
        val inputStream = FileInputStream(tempFile)
        val outputStream = FileOutputStream(file)

        val buffer = ByteArray(inputStream.available())
        while (inputStream.read(buffer) != -1) {
            outputStream.write(buffer)
        }
        outputStream.close()
        inputStream.close()
        with(tempFile) { delete() }
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

fun retrieveKeyPair(vaultName: String): PGPSecretKeyRing? {
    val file: File = File(System.getProperty(Enums.HOME_DIR.value) + "/${Enums.APP_DIRECTORY.value}/$vaultName.asc")
    try {
        if (file.exists()) {
            val privateKey: ByteArray = Files.readAllBytes(file.toPath())
            val privateKeyObj: PGPSecretKeyRing? = PGPainless.readKeyRing().secretKeyRing(privateKey)
            if (privateKeyObj != null) {
                return privateKeyObj
            }
        }
    } catch (e: PGPException) {
        println(e.printStackTrace())
        return null
    }
    return null
}

fun listAllKeys(): MutableList<String> {
    val directoryPath = File(
        System.getProperty(Enums.HOME_DIR.value + "/" + Enums.APP_DIRECTORY.value + "/${Enums.KEY_DIR.value}")
    )
    val fileList = mutableListOf<String>()
    if (directoryPath.exists() && directoryPath.isDirectory()) {
        directoryPath.walkTopDown().forEach {
            if (it.isFile) {
                fileList.add(it.name)
            }
        }
        return fileList
    }
    return fileList

}

