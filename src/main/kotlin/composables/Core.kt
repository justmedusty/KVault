package composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import enums.Enums
import fileio.*
import java.io.File

@Composable
@Preview
fun app() {

    MaterialTheme(
        typography = Typography(defaultFontFamily = FontFamily.Monospace)
    ) {
        core()
    }
}

@Composable
fun core() {
    val selectedItem by remember { mutableStateOf("My Vaults") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var fileList by remember { mutableStateOf(emptyList<File>()) }
    var vaultName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf(false) }
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
                onVaultOpened = { vaultname, pwd, files ->
                    vaultName = vaultname
                    password = pwd
                    fileList = files
                })
            IconButton(onClick = {
                isDialogOpen = !isDialogOpen
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Vault")

            }
            IconButton(onClick = {
                info = !info
            }) {
                Icon(Icons.Filled.Info, contentDescription = "Information")
            }

        })
        if (isDialogOpen) newVaultForm(onDismiss = { isDialogOpen = false })
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).background(color = Color.Transparent),
        ) {


            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (info) {
                    information(onDismissRequest = { info = false })
                }


                if (password.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 5.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Files in Vault '$vaultName':", fontWeight = FontWeight.Black, fontSize = 18.sp
                            )
                            Text("Click a file name to open it", modifier = Modifier.padding(bottom = 15.dp))



                        }
                    }


                    Divider(Modifier.border(10.dp, Color.Black))
                    if (fileList.isNotEmpty() && isDirectoryEncrypted(System.getProperty(Enums.HOME_DIR.value) + Enums.APP_DIRECTORY.value + Enums.VAULTS_DIR.value + "/$vaultName")) {
                        Text(
                            "Your password was incorrect!",
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(fraction = 0.55f).padding(start = 16.dp).weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
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
                                        Row {

                                            Button(
                                                onClick = { openFile(vaultName, file.name) },
                                                modifier = Modifier.padding(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    backgroundColor = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Text(file.name, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                        Divider()
                                    }


                                }

                            }

                        }
                        Divider(Modifier.border(10.dp, Color.Black))
                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top= 15.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 5.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (fileList.isNotEmpty()) {
                                    Button(onClick = {
                                        closeVault(vaultName, password)
                                        password = ""
                                        fileList = emptyList()
                                    }) {

                                        Text("Close Vault")
                                    }
                                }
                                Button(onClick = {
                                    openVaultInExplorer(vaultName)
                                }, modifier = Modifier.padding(start = 10.dp)) {

                                    Text("Open Folder")
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


                } else {
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Box(
                            modifier = Modifier.padding(all = 175.dp)
                                .background(color = Color.LightGray, shape = RoundedCornerShape(20.dp))
                        ) {
                            Box(modifier = Modifier.padding(all = 10.dp)) {
                                Text(
                                    "Open Or Create A Vault To Get Started",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                    }

                }
            }
        }

    }
}

@Composable
fun information(onDismissRequest: () -> Unit) {
    val openAlertDialog = remember { mutableStateOf(true) }

    when {
        openAlertDialog.value -> {
            infoDialog(
                onDismissRequest = {
                    openAlertDialog.value = false
                    onDismissRequest()
                },
                dialogTitle = "About",
                dialogText = "This app generates password locked PGP keys for you to keep your files safe. You can click on files to open them, you can open the folder as well and add files while it is open." + " Directories are recursively encrypted so you can store as many nested directories as you would like in your vault. Something important to consider, if you do not click close vault and let it fully finish, " + "your files will not be fully encrypted. If you have large files it will be slow to encrypt/decrypt your vault. The app will freeze until it is finished, you must let it finish or else your files will be exposed."
            )
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

@Composable
fun infoDialog(
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(title = {
        Text(text = dialogTitle)
    }, text = {
        Text(text = dialogText)
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {

    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text("Dismiss")
        }
    })
}
