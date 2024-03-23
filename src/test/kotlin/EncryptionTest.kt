import encryption.decryptDirectory
import encryption.encryptDirectory
import enums.Enums
import fileio.*
import kotlinx.coroutines.delay
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
    private val test123 =
        File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/test123")
    private val passphrase = "12345678"
    val privateKey = retrieveKeyPair("TestVault")


    @Test
    fun createVaultWithNewKeyPair() {
        createVault("TestVault", "12345678")
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
    fun testWindowsErrorLockout() {
        val password = "test123"
        val vault = "test123"
        var failureCount = 0
        var runCount = 0

        repeat(100){
            Thread.sleep(2500)
            closeVault(vault, password)
           Thread.sleep(2500)
            openVault(vault, password)

            Thread.sleep(1500)
            if (!isDirectoryEncryptedTest(test123.absolutePath)) {
                failureCount++
                println("FAILURE FAILURE FAILURE\nFAILURE FAILURE FAILURE\n")
            }
            runCount ++
            println(runCount)


        }
        assertTrue(failureCount < 10)
        assertTrue(runCount == 100)
        println("Failure count is $failureCount")
    }

}
