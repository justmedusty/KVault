package composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import enums.Enums
import fileio.createVault
import fileio.openVault
import java.io.File

@Composable
fun newVaultForm(
    onDismiss: () -> Unit
) {
    var vaultName by remember { mutableStateOf("") }
    var dismissed by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf(false) }
    if (dismissed) {
        onDismiss()
    }

    fun submitNewVault(): String {
        if (vaultName == "") return "Vault name cannot be empty"
        if (password == "") return "Password cannot be empty"

        return when {
            createVault(vaultName, password) -> {
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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

                TextField(
                    value = vaultName,
                    onValueChange = { vaultName = it },
                    label = { Text("Vault Name") },
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
    onVaultOpened: (String, String, List<File>) -> Unit, // Callback function to pass the file list to the parent
    onDismiss: () -> Unit // Callback function to dismiss the dialog
) {
    var dismissed by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var fileList: List<File> by remember { mutableStateOf(emptyList()) }
    if (dismissed) {
        onDismiss()
        return
    }
    if(fileList.isNotEmpty()){

        Dialog(onDismissRequest = { dismissed = true }) {
            Surface {
                Column(modifier = Modifier.padding(16.dp),verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password for vault $vaultName") },
                        modifier = Modifier.padding(top = 10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Row(modifier = Modifier.padding(top = 10.dp)) {
                        Button(onClick = {
                            fileList = openVault(vaultName, password)
                            println(fileList)
                            onVaultOpened(vaultName, password, fileList)
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
    }else onDismiss()

}