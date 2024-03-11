package composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun newVaultForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dismissed by remember{ mutableStateOf(false) }
    if (dismissed) {
        return
    }
    Dialog(onDismissRequest = {}) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(modifier = Modifier.padding(top = 10.dp)){
                    Button(onClick = { }) {
                        Text("Submit")
                    }

                    Button(onClick = {dismissed = true }) {
                        Text("Cancel")
                    }
                }

            }

        }

    }
}