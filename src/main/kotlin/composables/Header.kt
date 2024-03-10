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

        Column {
            TopAppBar(
                backgroundColor = Color.Black,
                contentColor = Color.White,
                title = {
                    Image(
                        painter = BitmapPainter(useResource("vault.png", ::loadImageBitmap)),
                        contentDescription = "Logo",
                        modifier = Modifier.size(46.dp)
                    )
                    Text("KVault")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                actions = {
                    DropdownList(
                        dropdownItems = listOf("My Vaults", "Another Option", "Yet Another Option"),
                        selectedItem = remember { mutableStateOf(selectedItem) }
                    )
                    IconButton(onClick = { /* Create Vault action */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Create Vault")
                    }
                }
            )
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your application content goes here")
            }
        }
    }
}

@Composable
fun DropdownList(dropdownItems: List<String>, selectedItem: MutableState<String>) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            dropdownItems.forEach { item ->
                DropdownMenuItem(onClick = {
                    selectedItem.value = item
                    expanded = false
                }) {
                    Text(item)
                }
            }
        }
    }
}