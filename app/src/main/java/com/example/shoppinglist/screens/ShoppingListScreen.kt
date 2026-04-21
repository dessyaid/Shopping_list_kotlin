package com.example.shoppinglist.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.components.*
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

    if (tabsState == null) {
        ShoppingCartLoading()
        return
    }

    val tabs = tabsState!!
    val currentTab = tabs.find { it.id == selectedTabId }

    Scaffold(
        topBar = {
            ShoppingListTopAppBar(
                currentTab = currentTab,
                onAddTab = { showAddTabDialog = true },
                onRenameTab = { showRenameTabDialog = it },
                onArchiveTab = { viewModel.toggleArchiveTab(it) },
                onDeleteTab = { viewModel.deleteTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (tabs.isNotEmpty()) {
                TabSelector(
                    tabs = tabs,
                    selectedTabId = selectedTabId,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }

            if (selectedTabId != null) {
                ShoppingItemsList(
                    shoppingList = shoppingList,
                    isArchived = currentTab?.isArchived ?: false,
                    onAddItem = { name, price, desc -> viewModel.addItem(name, price, desc) },
                    onToggleBought = { viewModel.toggleBought(it) },
                    onDelete = { item ->
                        viewModel.deleteItem(item)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Item deleted",
                                actionLabel = "Cancel",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreItem(item)
                            }
                        }
                    },
                    onUpdateItem = { item, name, price, desc ->
                        viewModel.updateItem(item.copy(name = name, price = price, description = desc))
                    }
                )
            } else {
                EmptyState()
            }
        }
    }

    if (showAddTabDialog) {
        TabNameDialog(
            title = "New Tab",
            onDismiss = { showAddTabDialog = false },
            onConfirm = { name ->
                viewModel.addTab(name)
                showAddTabDialog = false
            }
        )
    }

    showRenameTabDialog?.let { tab ->
        TabNameDialog(
            title = "Rename Tab",
            initialName = tab.title,
            onDismiss = { showRenameTabDialog = null },
            onConfirm = { newName ->
                viewModel.renameTab(tab, newName)
                showRenameTabDialog = null
            }
        )
    }
}
