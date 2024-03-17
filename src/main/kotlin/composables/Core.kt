package composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import enums.Enums
import fileio.addFileToVault
import fileio.closeVault
import fileio.isDirectoryEncrypted
import fileio.listAllVaults
import java.io.File

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
        var fileList by remember { mutableStateOf(emptyList<File>()) }
        var vaultName by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }
        var selectedFilePath by remember { mutableStateOf("") }

        Column {
            TopAppBar(backgroundColor = Color.Black, contentColor = Color.White, title = {
                Image(
                    painter = BitmapPainter(useResource("vault.png", ::loadImageBitmap)),
                    contentDescription = "Logo",
                    modifier = Modifier.size(46.dp)
                )
                Text("KVault")
            }, modifier = Modifier.align(Alignment.CenterHorizontally), actions = {
                dropdownList(dropdownItems = listAllVaults(),
                    selectedItem = remember { mutableStateOf(selectedItem) },
                    onVaultOpened = { vltnme, pwd, files ->
                        vaultName = vltnme
                        password = pwd
                        fileList = files
                    })
                IconButton(onClick = {
                    isDialogOpen = !isDialogOpen
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Vault")
                }
            })
            if (isDialogOpen) newVaultForm(onDismiss = { isDialogOpen = false })
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                if (password.isNotEmpty()) {

                    Text("Files in Vault $vaultName:")
                    if (fileList.isNotEmpty() && isDirectoryEncrypted(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")) {
                        Text("Your password was incorrect!")
                    } else if (fileList.isEmpty()) {
                        Text("No files yet!")
                    } else {
                        LazyColumn {
                            Modifier.align(Alignment.CenterHorizontally)
                            item {
                                fileList.forEach { file ->
                                    if (file.isDirectory) {
                                        Text(file.name + " (Directory)")
                                    } else {
                                        Text(file.name)
                                    }
                                }
                            }
                        }
                        Button(onClick = {
                            closeVault(vaultName, password)
                            password = ""
                            fileList = emptyList()
                        }) {
                            Text("Close Vault")
                        }

                        Button(
                            onClick = { showDialog = !showDialog }, modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Open File Picker")
                        }

                        selectedFilePath.let {
                            Text("Selected File Path: $it")
                        }

                        filePickerDialog(showDialog = mutableStateOf(showDialog),onDismiss = {showDialog = false} , vaultName)
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
    onVaultOpened: (String, String, List<File>) -> Unit // Callback function to pass password and file list to core
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var vaultName by remember { mutableStateOf("") }
    if (isDialogOpen) {
        openVaultForm(vaultName = vaultName,
            onVaultOpened = { _, password, files -> onVaultOpened(vaultName, password, files) },
            onDismiss = { isDialogOpen = false })
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