package fileio

import encryption.decryptDirectory
import encryption.encryptDirectory
import encryption.generateKeyPair
import enums.Enums
import kotlinx.coroutines.runBlocking
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPSecretKeyRing
import java.awt.Desktop
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name


fun createVault(vaultName: String, password: String): Boolean {
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName".trim())

    if (!directory.exists()) {
        val created = directory.mkdirs()

        if (created) {
            generateKeyPair(password, vaultName)

            val secretKey: PGPSecretKeyRing? = retrieveKeyPair(vaultName)

            try {
                return when (secretKey) {
                    null -> {
                        println("Failed to retrieve secret key.")
                        false
                    }

                    else -> {
                        encryptDirectory(directory.toPath().toString(), secretKey, password)
                        true
                    }
                }
            } catch (e: Exception) {
                println("ERROR ENCRYPTING DIRECTORY: ${e.message}")
                return false
            }
        } else {
            println("Failed to create directory.")
            return false
        }
    } else {
        println("Vault directory already exists.")
        return false
    }
}

fun listAllVaults(): List<String> {
    val directory = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value)
    val vaultList = (directory.listFiles()?.toMutableList())
    val responseList = mutableListOf<String>()
    if (!vaultList.isNullOrEmpty()) {
        for (file in vaultList) {
            if (file.isDirectory() && file.extension != ".gpg") {
                responseList.add(file.name)
            }
        }
    } else {
        responseList.add("You have no vaults")
    }


    return responseList
}

fun updateFileList(vaultName: String): List<File> {
    val fileList = mutableListOf<File>()
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    directory.listFiles()?.forEach { file ->
        fileList.add(file)
    }
    return fileList
}

fun openVault(vaultName: String, password: String): List<File> {

    //This Thread.sleep is required because motherfucking microsoft likes to completely lock a file when in use by a process and deny any attempt at deletion
    //Not required on linux because there is a queue for actions while a process has a lock on the file and it will just exec afterward
    Thread.sleep(2000)

    val fileList = mutableListOf<File>()
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    val privateKey: PGPSecretKeyRing? = retrieveKeyPair(vaultName)
    if (privateKey != null && directory.exists()) {
        try {
            decryptDirectory(directory.toString(), privateKey, password)
            directory.listFiles()?.forEach { file ->

                //The way the windows kernel handles file locks means that if there is a request to delete the file while a process is still accessing the file it will deny
                //Whereas linux will add this to queue to be executed once the file has been freed up. Hence, this will help mask that issue on Windows computers. It only applies
                //To decryption, so it is safe just a visual nuisance
                if (!file.name.contains("gpg")) {
                    fileList.add(file)
                }
            }


            return fileList
        } catch (e: Exception) {
            println(e.printStackTrace())
            return emptyList()
        }
    } else {
        return emptyList()
    }

}

fun closeVault(vaultName: String, password: String): Boolean {
    val vault =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    val privateKey: PGPSecretKeyRing? = retrieveKeyPair(vaultName)
    try {
        if (privateKey != null) {
            encryptDirectory(vault.toPath().toString(), privateKey, password)
            return true
        }
    } catch (e: PGPException) {
        println(e.message)
        return false
    }

    return false
}

fun isDirectoryEncrypted(directoryPath: String): Boolean {
    val vault = File(directoryPath)
    val files: List<File>? = vault.listFiles()?.toList()

    if (files != null) {
        for (file in files) {
            if (file.isDirectory) {
                isDirectoryEncrypted(file.absolutePath)
            }
            if (!file.isDirectory && !file.name.endsWith(".gpg")) {
                return false
            }
        }
    }
    return true
}

fun isDirectoryEncryptedTest(directoryPath: String): Boolean {
    val vault = File(directoryPath)
    val files: List<File>? = vault.listFiles()?.toList()
    return if (files != null) {
        files.size == 6
    } else false
}


fun addFileToVault(path: String, vaultName: String): Boolean {
    val file = File(path)

    val destinationVault =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")

    return if (destinationVault.exists() && file.exists()) {
        val destinationFile = File(destinationVault, file.name)
        file.copyTo(destinationFile, overwrite = true)
        with(file) {
            delete()
        }
        true
    } else {
        false

    }
}

fun isVaultEmpty(vaultName: String): Boolean {
    val vaultPath =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName").toPath()
    val vaultList: List<Path> = Files.walk(vaultPath).toList()
    return vaultList.isEmpty()
}

fun getFileCount(vaultName: String): Int {
    val vault =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName").toPath()
    var fileCount = 0
    for (file in vault) {
        if (!file.isDirectory()) {
            fileCount++
        }
    }
    return fileCount
}

fun openVaultInExplorer(vaultName: String) {
    val vaultPath =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(File(vaultPath.absolutePath))
    }
}

fun openFile(vaultName: String, fileName: String) {
    val filePath =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName/$fileName")
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(File(filePath.absolutePath))
    }
}

//Going to try this function to deal with the snagglers that get left on windows when deletion isn't possible
fun deleteAllDoubleFiles(vaultName: String) {
    val vault =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    if (!isDirectoryEncryptedTest(vaultName)) {
        Files.walk(vault.toPath()).filter { file -> file.name.endsWith(".gpg") }
            .forEach { file -> Files.deleteIfExists(file) }
    }
}