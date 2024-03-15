import encryption.decryptDirectory
import encryption.encryptDirectory
import enums.Enums
import fileio.createVault
import fileio.isDirectoryEncrypted
import fileio.listAllKeys
import fileio.retrieveKeyPair
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class EncryptionTest {
    private val testFolder =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/TestVault")
    private val outputFile =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/TestVault/test.gpg")
    private val testFile =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/TestVault/test.txt")
    private val passphrase = "12345678"
    val privateKey = retrieveKeyPair("TestVault")


    @Test
    fun createVaultWithNewKeyPair() {
        createVault("TestVault", "dusty", "12345678", "dustyn@dustyn.com")
        assertTrue(isDirectoryEncrypted("TestVault"))
    }

    @Test
    fun testEncryptionDir() {
        val privateKey = retrieveKeyPair("TestVault")
        testFolder.mkdirs()
        val fileToEncrypt = testFolder
        val passphrase = "12345678"
        assertNotNull(privateKey)
        if (privateKey != null) {
            encryptDirectory(testFolder.toString(), privateKey, passphrase)
            assertNotNull(privateKey)
            assertTrue(isDirectoryEncrypted(testFolder.absolutePath))

        }


    }

    @Test
    fun decryptDirectory() {
        val folder = testFolder.toPath().toString()
        if (privateKey != null) {
            decryptDirectory(folder, privateKey, passphrase)
        }
        assertTrue(!isDirectoryEncrypted(testFolder.absolutePath))

    }

    @Test
    fun listAllKeysTest() {
        val list = listAllKeys()
        assertNotNull(list)
    }


    /*
        @Test
        fun testEncryptionWithInvalidPublicKey() {
            val inputFile = File("input.txt")
            val outputFile = File("output.gpg")
            val publicKey = null // Assuming we don't have a valid public key for this test
            val passphrase = "your_passphrase"

            val result = encryptFile(inputFile, outputFile, publicKey, passphrase)

            assertNotEquals("Success!", result)
            assertFalse(outputFile.exists())
        }

        @Test
        fun testEncryptionWithInvalidPassphrase() {
            val inputFile = File("input.txt")
            val outputFile = File("output.gpg")
            val publicKey = getPublicKey() // You need to implement this method
            val passphrase = "invalid_passphrase"

            val result = encryptFile(inputFile, outputFile, publicKey, passphrase)

            assertNotEquals("Success!", result)
            assertFalse(outputFile.exists())
        }
        */
}
