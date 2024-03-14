package composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import fileio.listAllVaults

@Composable
@Preview
fun app() {

    MaterialTheme {
        core()
    }
}

@Composable
fun core() {
    MaterialTheme {
        var selectedItem by remember { mutableStateOf("My Vaults") }
        var isDialogOpen by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var fileList by remember { mutableStateOf(emptyList<String>()) }

        Column {
            TopAppBar(backgroundColor = Color.Black, contentColor = Color.White, title = {
                Image(
                    painter = BitmapPainter(useResource("vault.png", ::loadImageBitmap)),
                    contentDescription = "Logo",
                    modifier = Modifier.size(46.dp)
                )
                Text("KVault")
            }, modifier = Modifier.align(Alignment.CenterHorizontally), actions = {
                dropdownList(
                    dropdownItems = listAllVaults(),
                    selectedItem = remember { mutableStateOf(selectedItem) },
                    onVaultOpened = { pwd, files ->
                        password = pwd
                        fileList = files
                    }
                )
                IconButton(onClick = {
                    isDialogOpen = !isDialogOpen
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Vault")
                }
            })
            if (isDialogOpen) newVaultForm()
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (password.isNotEmpty() && fileList.isNotEmpty()) {
                    Text("Vault Password: $password")
                    Text("Files in Vault:")
                    fileList.forEach { file ->
                        Text(file)
                    }
                } else {
                    Text("Open Or Create A Vault To Get Started")
                }
            }
        }
    }
}

@Composable
fun dropdownList(
    dropdownItems: List<String>,
    selectedItem: MutableState<String>,
    onVaultOpened: (String, List<String>) -> Unit // Callback function to pass password and file list to core
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var vaultName by remember { mutableStateOf("") }
    var fileList by remember { mutableStateOf(emptyList<String>()) }
    if (isDialogOpen){
        openVaultForm(
            vaultName = vaultName,
            onVaultOpened = { password, files -> onVaultOpened(password, files) },
            onDismiss = { isDialogOpen = false }
        )
    }
    Column {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
        }

        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()
        ) {
            dropdownItems.forEach { item ->
                DropdownMenuItem(onClick = {
                    selectedItem.value = item
                    expanded = false
                    vaultName = selectedItem.value
                    isDialogOpen = true
                }) {
                    Text(item)
                }
            }
        }
    }
}