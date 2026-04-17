package com.example.shoppinglist.screens

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.components.AddItemButton
import com.example.shoppinglist.components.ShoppingItemCard
import com.example.shoppinglist.viewmodel.ShoppingListViewModel
import com.example.shoppinglist.viewmodel.ShoppingListViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = viewModel(
    factory = ShoppingListViewModelFactory(LocalContext.current
        .applicationContext as Application)
)) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                AddItemButton { viewModel.addItem(it) }
            }
            itemsIndexed(viewModel.shoppingList) { _, item ->
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShoppingListScreenPreview() {
    ShoppingListScreen()
}
