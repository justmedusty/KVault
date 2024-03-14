package composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fileio.createVault
import fileio.openVault

@Composable
fun newVaultForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var vaultName by remember { mutableStateOf("") }
    var dismissed by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf(false) }
    if (dismissed) {
        return
    }

    fun submitNewVault(): String {
        if (name == null) return "Name is required (Can use the same name as others)"
        if (email == null) return "Email is required (Can use the same name as others)"
        if (vaultName == null) return "Vault name cannot be empty"
        if (password == null) return "Password cannot be empty"

        return when {
            createVault(vaultName, name, email, password) -> {
                dismissed = true
                "Success"
            }

            else -> {
                "An error occurred "
            }
        }


    }
    Dialog(onDismissRequest = { return@Dialog }) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {

                TextField(
                    value = vaultName,
                    onValueChange = { vaultName = it },
                    label = { Text("Vault Name") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name For Keypair") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email For KeyPair") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password For KeyPair/Vault") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(modifier = Modifier.padding(top = 10.dp)) {
                    Button(onClick = {
                        statusMessage = submitNewVault()
                        notification = true
                    }) {
                        Text("Submit")
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                    Button(onClick = { dismissed = !dismissed }) {
                        Text("Cancel")
                    }
                }

            }

        }

    }
}

@Composable
fun openVaultForm(
    vaultName: String,
    onVaultOpened: (String, List<String>) -> Unit, // Callback function to pass the file list to the parent
    onDismiss: () -> Unit // Callback function to dismiss the dialog
) {
    var dismissed by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var fileList: List<String> by remember { mutableStateOf(emptyList()) }
    if (dismissed) {
        onDismiss()
        return
    }

    Dialog(onDismissRequest = { dismissed = true }) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password for vault $vaultName")},
                    modifier = Modifier.padding(top = 10.dp),
                    visualTransformation = PasswordVisualTransformation(),
                )
                Row(modifier = Modifier.padding(top = 10.dp)) {
                    Button(onClick = {
                        fileList = openVault(vaultName, password)
                        onVaultOpened(password, fileList)
                        dismissed = true
                    }) {
                        Text("Submit")
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                    Button(onClick = { dismissed = true }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}