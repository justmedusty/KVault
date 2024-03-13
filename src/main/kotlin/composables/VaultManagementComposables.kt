package composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fileio.createVault

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

       if (createVault(vaultName, name, email, password)) {
           dismissed = true
          return  "Success"
        } else return "An error occurred "


    }
    Dialog(onDismissRequest = {}) {
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
                    Button(onClick = { statusMessage = submitNewVault()
                    notification = true }) {
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


