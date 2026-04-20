package com.example.shoppinglist.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.components.AddItemButton
import com.example.shoppinglist.components.ShoppingCartLoading
import com.example.shoppinglist.components.ShoppingItemCard
import com.example.shoppinglist.model.TabItem
import com.example.shoppinglist.viewmodel.ShoppingListViewModel
import com.example.shoppinglist.viewmodel.ShoppingListViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = viewModel(
    factory = ShoppingListViewModelFactory(LocalContext.current
        .applicationContext as Application)
)) {
    val tabsState by viewModel.tabs.collectAsStateWithLifecycle()
    val selectedTabId by viewModel.selectedTabId.collectAsStateWithLifecycle()
    val shoppingList by viewModel.shoppingList.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddTabDialog by remember { mutableStateOf(false) }
    var showRenameTabDialog by remember { mutableStateOf<TabItem?>(null) }
    var showTabMenu by remember { mutableStateOf(false) }

    if (tabsState == null) {
        ShoppingCartLoading()
    } else {
        val tabs = tabsState!!
        val currentTab = tabs.find { it.id == selectedTabId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTab?.title ?: "Shopping List") },
                    actions = {
                        IconButton(onClick = { showAddTabDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tab")
                        }
                        if (currentTab != null) {
                            Box {
                                IconButton(onClick = { showTabMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Tab Settings")
                                }
                                DropdownMenu(
                                    expanded = showTabMenu,
                                    onDismissRequest = { showTabMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename Tab") },
                                        onClick = {
                                            showRenameTabDialog = currentTab
                                            showTabMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Tab") },
                                        onClick = {
                                            viewModel.deleteTab(currentTab)
                                            showTabMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if (tabs.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0),
                        edgePadding = 16.dp
                    ) {
                        tabs.forEach { tab ->
                            Tab(
                                selected = selectedTabId == tab.id,
                                onClick = { viewModel.selectTab(tab.id) },
                                text = { Text(tab.title) }
                            )
                        }
                    }
                }

                if (selectedTabId != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            AddItemButton { viewModel.addItem(it) }
                        }
                        itemsIndexed(shoppingList) { _, item ->
                            ShoppingItemCard(
                                item = item,
                                onToggleBought = { viewModel.toggleBought(item) },
                                onDelete = {
                                    val deletedItem = item
                                    viewModel.deleteItem(item)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Item deleted",
                                            actionLabel = "Cancel",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreItem(deletedItem)
                                        }
                                    }
                                },
                                onUpdateName = { newName ->
                                    viewModel.updateItem(item.copy(name = newName))
                                }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Create a tab to start")
                    }
                }
            }
        }

        if (showAddTabDialog) {
            var tabName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddTabDialog = false },
                title = { Text("New Tab") },
                text = {
                    OutlinedTextField(
                        value = tabName,
                        onValueChange = { tabName = it },
                        label = { Text("Tab Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (tabName.isNotBlank()) {
                            viewModel.addTab(tabName)
                            showAddTabDialog = false
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTabDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showRenameTabDialog != null) {
            var newName by remember { mutableStateOf(showRenameTabDialog?.title ?: "") }
            AlertDialog(
                onDismissRequest = { showRenameTabDialog = null },
                title = { Text("Rename Tab") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renameTab(showRenameTabDialog!!, newName)
                            showRenameTabDialog = null
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameTabDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}
