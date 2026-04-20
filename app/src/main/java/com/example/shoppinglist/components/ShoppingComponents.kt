package com.example.shoppinglist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppinglist.model.ShoppingItem

@Composable
fun AddItemButton(addItem: (String) -> Unit = {}) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(bottom = 16.dp))  {
        OutlinedTextField(
            value = text,
            onValueChange = {text = it },
            label = { Text("Add Item") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (text.isNotEmpty()) {
                    addItem(text)
                    text = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add")
        }
    }
}

@Composable
fun DeleteIconButton(onDelete: () -> Unit) {
    IconButton(onClick = onDelete) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Gray
        )
    }
}

@Composable
fun SettingsIconButton(onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Change item") },
                onClick = {
                    onEdit()
                    showMenu = false
                }
            )
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onToggleBought: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUpdateName: (String) -> Unit = {},
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(item.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (item.isBought) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray,
                MaterialTheme.shapes.large
            )
            .clickable { if (!isEditing) onToggleBought() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEditing) {
            Checkbox(checked = item.isBought, onCheckedChange = {
                onToggleBought()
            })
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                textDecoration = if (item.isBought)
                    TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isBought) Color.Gray else Color.Black
            )
            
            DeleteIconButton(onDelete = onDelete)
            SettingsIconButton(onEdit = { isEditing = true })

        } else {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = {
                if (editedName.isNotBlank()) {
                    onUpdateName(editedName)
                    isEditing = false
                }
            }) {
                Icon(Icons.Default.Check,
                    contentDescription = "Save", tint = Color.Green)
            }
        }
    }
}

@Composable
fun ShoppingCartLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp),
            shadowElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier
                    .padding(30.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
