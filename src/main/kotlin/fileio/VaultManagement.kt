package fileio

import encryption.encryptDirectory
import encryption.generateKeyPair
import enums.Enums
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.util.encoders.Base64
import org.pgpainless.key.generation.type.rsa.RsaLength
import java.io.File


fun createVault(vaultName: String, username: String, password: String, email: String, rsaLength: RsaLength): Boolean {
    val directory = File(System.getProperty(Enums.HOME_DIR.value) + Enums.VAULTS_DIR + "/$vaultName")
    if (!directory.exists()) {

        val created = directory.mkdirs()
        if (created) {
            generateKeyPair(password, username, email, rsaLength, vaultName)
            val secretKey: PGPSecretKeyRing? =
                retrieveKeyPair(Base64.encode(vaultName.toByteArray()).contentToString() + ".asc")
            try {
                if (secretKey != null) {
                    //this might not work pytting this comment here in case it doenst and i need to find this line
                    encryptDirectory(directory.toString(), secretKey.publicKey.toString(), password)
                } else return false
            } catch (e: Exception) {
                println("ERROR ENCRYPTING DIRECTORY")
            }
        }
        return created
    }
    return false
}
