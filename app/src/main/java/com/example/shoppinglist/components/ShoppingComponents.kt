package com.example.shoppinglist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppinglist.model.ShoppingItem
import com.example.shoppinglist.model.TabItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListTopAppBar(
    currentTab: TabItem?,
    onAddTab: () -> Unit,
    onRenameTab: (TabItem) -> Unit,
    onArchiveTab: (TabItem) -> Unit,
    onDeleteTab: (TabItem) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currentTab?.title ?: "Shopping List")
                if (currentTab?.isArchived == true) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            "Archived",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onAddTab) {
                Icon(Icons.Default.Add, contentDescription = "Add Tab")
            }
            if (currentTab != null) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Tab Settings")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename Tab") },
                            onClick = {
                                onRenameTab(currentTab)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (currentTab.isArchived) "Unarchive Tab" else "Archive Tab") },
                            onClick = {
                                onArchiveTab(currentTab)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Tab") },
                            onClick = {
                                onDeleteTab(currentTab)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TabSelector(
    tabs: List<TabItem>,
    selectedTabId: Int?,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0),
        edgePadding = 16.dp
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTabId == tab.id,
                onClick = { onTabSelected(tab.id) },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tab.title)
                        if (tab.isArchived) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp).padding(start = 4.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ShoppingItemsList(
    shoppingList: List<ShoppingItem>,
    isArchived: Boolean,
    onAddItem: (String, Double?, String?) -> Unit,
    onToggleBought: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onUpdateItem: (ShoppingItem, String, Double?, String?) -> Unit,
    onUnarchive: () -> Unit = {}
) {
    val hasPrices = shoppingList.any { it.price != null }
    val totalSum = shoppingList.sumOf { it.price ?: 0.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isArchived) {
            item {
                AddItemButton { name, price, desc -> onAddItem(name, price, desc) }
            }
        }

        itemsIndexed(shoppingList) { _, item ->
            ShoppingItemCard(
                item = item,
                onToggleBought = { onToggleBought(item) },
                onDelete = { onDelete(item) },
                onUpdateItem = { name, price, desc -> onUpdateItem(item, name, price, desc) }
            )
        }
        if (hasPrices) {
            item {
                TotalSumRow(totalSum)
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Create a tab to start")
    }
}

@Composable
fun TabNameDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tab Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name)
                }
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddItemButton(addItem: (String, Double?, String?) -> Unit = { _, _, _ -> }) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(bottom = 16.dp))  {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (name.isNotEmpty()) {
                    val priceDouble = price.toDoubleOrNull()
                    addItem(name, priceDouble, description.ifBlank { null })
                    name = ""
                    price = ""
                    description = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add")
        }
    }
}

@Composable
fun ShoppingVerticalDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .fillMaxHeight()
            .width(1.dp)
            .background(Color.Gray.copy(alpha = 0.7f), CircleShape)
    )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onToggleBought: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUpdateItem: (String, Double?, String?) -> Unit = { _, _, _ -> },
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDescription by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(item.name) }
    var editedPrice by remember { mutableStateOf(item.price?.toString() ?: "") }
    var editedDescription by remember { mutableStateOf(item.description ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (item.isBought) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray,
                MaterialTheme.shapes.large
            )
            .combinedClickable(
                onClick = { if (!isEditing) onToggleBought() },
                onLongClick = { if (!isEditing) showDescription = !showDescription }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditing) {
                Checkbox(checked = item.isBought, onCheckedChange = {
                    onToggleBought()
                })
                
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isBought)
                        TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isBought) Color.Gray else Color.Black
                )

                if (item.price != null) {
                    ShoppingVerticalDivider()
                    Text(
                        text = "${item.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                ShoppingVerticalDivider()
                DeleteIconButton(onDelete = onDelete)
                
                ShoppingVerticalDivider()
                SettingsIconButton(onEdit = { 
                    editedName = item.name
                    editedPrice = item.price?.toString() ?: ""
                    editedDescription = item.description ?: ""
                    isEditing = true 
                })

            } else {
                Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editedPrice,
                            onValueChange = { editedPrice = it },
                            label = { Text("Price") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(onClick = {
                    if (editedName.isNotBlank()) {
                        onUpdateItem(
                            editedName,
                            editedPrice.toDoubleOrNull(),
                            editedDescription.ifBlank { null }
                        )
                        isEditing = false
                    }
                }) {
                    Icon(Icons.Default.Check,
                        contentDescription = "Save", tint = Color.Green)
                }
            }
        }
        
        AnimatedVisibility(visible = showDescription && !isEditing && !item.description.isNullOrBlank()) {
            Column(modifier = Modifier.padding(start = 48.dp, top = 4.dp, bottom = 8.dp)) {
                Text(
                    text = item.description ?: "",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun TotalSumRow(total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total: ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = String.format(Locale.getDefault(), "%.2f", total),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
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
