package fileio

import encryption.decryptDirectory
import encryption.encryptDirectory
import encryption.generateKeyPair
import enums.Enums
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPSecretKeyRing
import java.io.File


fun createVault(vaultName: String, username: String, password: String, email: String): Boolean {
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName".trim())

    if (!directory.exists()) {
        val created = directory.mkdirs()

        if (created) {
            generateKeyPair(password, username, email, vaultName)

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
            if (file.isDirectory()) {
                responseList.add(file.name)
            }
        }
    } else {
        responseList.add("You have no vaults")
    }


    return responseList
}

fun openVault(vaultName: String, password: String): List<File> {
    val fileList = mutableListOf<File>()
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")
    val privateKey: PGPSecretKeyRing? = retrieveKeyPair(vaultName)
    if (privateKey != null && directory.exists()) {
        try {
            decryptDirectory(directory.toString(), privateKey, password)
            directory.listFiles()?.forEach { file ->
                fileList.add(file)
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
