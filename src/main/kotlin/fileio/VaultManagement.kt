package fileio

import encryption.decryptDirectory
import encryption.encryptDirectory
import encryption.generateKeyPair
import enums.Enums
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.key.generation.type.rsa.RsaLength
import java.io.File


fun createVault(vaultName: String, username: String, password: String, email: String, rsaLength: RsaLength): Boolean {
    val directory =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName".trim())

    if (!directory.exists()) {
        val created = directory.mkdirs()

        if (created) {
            generateKeyPair(password, username, email, rsaLength, vaultName)
            val secretKey: PGPSecretKeyRing? = retrieveKeyPair("$vaultName.asc")

            try {
                return when (secretKey) {
                    null -> {
                        println("Failed to retrieve secret key.")
                        false
                    }

                    else -> {
                        val encryptedDirectory = File(directory, "$vaultName.gpg")
                        encryptDirectory(directory.toPath().toString(), secretKey, password)
                        println(directory.toPath())
                        println(directory.toPath().toString())
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


fun openVault(vaultName: String, password: String): List<String> {
    val fileList = mutableListOf<String>()
    val directory = File(System.getProperty(Enums.HOME_DIR.value) + Enums.VAULTS_DIR + "/$vaultName")
    val privateKey: PGPSecretKeyRing? = retrieveKeyPair("$vaultName.asc")
    if (privateKey != null && directory.exists()) {
        try {
            decryptDirectory(directory.toString(), privateKey, password)
            directory.listFiles()?.forEach { file ->
                fileList.add(file.name)
            }
            return fileList
        } catch (e: Exception) {
            println(e.printStackTrace())
            return emptyList()
        }
    }
    return emptyList()

}

fun closeVault(vaultName: String, password: String): Boolean {
    val vault = File(System.getProperty(Enums.HOME_DIR.value) + Enums.VAULTS_DIR + "/$vaultName")
    val privateKey: PGPSecretKeyRing? = retrieveKeyPair("$vaultName.asc")
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

fun isDirectoryEncrypted(vaultName: String): Boolean {
    val vault = File(System.getProperty(Enums.HOME_DIR.value) + Enums.VAULTS_DIR + "/$vaultName")
    val files = vault.listFiles()
    files?.forEach { file ->
        println(file.name)
    }
    val list = mutableListOf<Boolean>()


    files?.forEach { file ->
        if (file.isFile) {
            if (file.name.contains( ".gpg")) {
                list.add(true)
            } else list.add(false)

        }
    }

    return list.all { true }
}
