import encryption.encryptFile
import fileio.createVault
import fileio.isDirectoryEncrypted
import fileio.retrieveKeyPair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pgpainless.key.generation.type.rsa.RsaLength
import java.io.File

class EncryptionTest {

    @Test
    fun createVaultWithNewKeyPair() {
        createVault("TestVault3", "dusty", "12345678", "dustyn@dustyn.com", RsaLength._4096)
        assertTrue(isDirectoryEncrypted("TestVault2"))
    }
/*
    @Test
    fun testEncryptionSuccess() {
        val inputFile = File("input.txt")
        val outputFile = File("output.gpg")
        val privateKey = retrieveKeyPair("TestVault")
        val passphrase = "1234"

        val result = encryptFile(inputFile, outputFile,privateKey, passphrase)

        assertEquals("Success!", result)
        assertTrue(outputFile.exists())
    }

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
