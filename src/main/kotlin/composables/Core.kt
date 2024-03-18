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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import enums.Enums
import fileio.*
import java.io.File
import javax.swing.text.StyledEditorKit.FontSizeAction

@Composable
@Preview
fun app() {

    MaterialTheme {
        core()
    }
}

@Composable
fun core() {
    var selectedItem by remember { mutableStateOf("My Vaults") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var fileList by remember { mutableStateOf(emptyList<File>()) }
    var vaultName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Column {
        TopAppBar(contentColor = Color.White, title = {
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
            modifier = Modifier.padding(16.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.Center,
        ) {


            if (password.isNotEmpty()) {

                Text("Files in Vault $vaultName:", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
                if (fileList.isNotEmpty() && isDirectoryEncrypted(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")) {
                    Text("Your password was incorrect!")
                } else if (fileList.isEmpty()) {
                    Text("No files yet!")
                    Button(
                        modifier = Modifier.padding(start = 10.dp),
                        onClick = { showDialog = !showDialog },
                    ) {
                        Text("Add File")
                    }

                    filePickerDialog(
                        showDialog = mutableStateOf(showDialog), onDismiss = {
                            showDialog = false
                            fileList = openVault(vaultName, password)

                        }, vaultName
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.align(Alignment.Start).fillMaxSize().defaultMinSize(0.dp, 0.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        item {
                            fileList.forEach { file ->
                                if (file.isDirectory) {
                                    Text(
                                        file.name + " (Directory)",
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else {
                                    Text(file.name, fontWeight = FontWeight.ExtraBold)
                                }
                                Divider()
                            }
                        }
                        item {
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)) {
                                Button(onClick = {
                                    closeVault(vaultName, password)
                                    password = ""
                                    fileList = emptyList()
                                }) {

                                    Text("Close Vault")
                                }

                                Button(onClick = {
                                    openVaultInExplorer(vaultName)
                                }, modifier = Modifier.padding(start = 10.dp)) {

                                    Text("Open Vault")
                                }

                                Button(
                                    modifier = Modifier.padding(start = 10.dp),
                                    onClick = { showDialog = !showDialog },
                                ) {
                                    Text("Add File")
                                }

                                filePickerDialog(
                                    showDialog = mutableStateOf(showDialog), onDismiss = {
                                        showDialog = false
                                        fileList = openVault(vaultName, password)

                                    }, vaultName
                                )

                            }
                        }
                    }





                }


            } else {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
            expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(IntrinsicSize.Max)
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