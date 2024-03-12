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

fun storeKeyPair(privateKey: ByteArray, vaultName: String) {
    val tempFile = File.createTempFile("temp_secret_key", ".asc")

    try {
        FileOutputStream(tempFile).use { output ->
            output.write(privateKey)
        }

        val applicationFolder = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + "/${Enums.KEY_DIR.value}")

        if (!applicationFolder.exists()) {
            applicationFolder.mkdirs()
        }

        val file = File(applicationFolder, "$vaultName.asc")

        FileInputStream(tempFile).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }

        tempFile.delete()
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

fun retrieveKeyPair(vaultName: String): PGPSecretKeyRing? {
    val file: File = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.KEY_DIR + "/${vaultName}.asc")
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

