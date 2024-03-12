import encryption.encryptFile
import enums.Enums
import fileio.createVault
import fileio.isDirectoryEncrypted
import fileio.retrieveKeyPair
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.pgpainless.key.generation.type.rsa.RsaLength
import java.io.File
import java.util.*

class EncryptionTest {
    val testFolder = File(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + "/test")

    @Test
    fun createVaultWithNewKeyPair() {
        createVault("TestVault3", "dusty", "12345678", "dustyn@dustyn.com", RsaLength._4096)
        assertTrue(isDirectoryEncrypted("TestVault2"))
    }

    @Test
    fun testEncryptionSuccess() {
        testFolder.mkdirs()
        val inputFile = File("$testFolder/input.txt")
        val outputFile = File("$testFolder/output.txt")
        val randomContent = UUID.randomUUID().toString()
        inputFile.writeText(randomContent)
        val privateKey = retrieveKeyPair("TestVault3")
        val passphrase = "12345678"
        assertNotNull(privateKey)
        if (privateKey != null) {
            val result = encryptFile(inputFile, outputFile, privateKey.publicKey.toString(), passphrase)


            assertEquals("Success!", result)
            assertTrue(outputFile.exists())
        }


    }/*
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
