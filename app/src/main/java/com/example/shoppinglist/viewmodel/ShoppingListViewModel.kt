package com.example.shoppinglist.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.dao.ShoppingDao
import com.example.shoppinglist.db.ShoppingDatabase
import com.example.shoppinglist.model.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShoppingListViewModel(application: Application): AndroidViewModel(application) {
    private val dao: ShoppingDao = ShoppingDatabase.getInstance(application).shoppingDao()
    private val _shoppingList = mutableStateListOf<ShoppingItem>()
    val shoppingList: List<ShoppingItem> get() = _shoppingList

    init {
        loadShoppingList()
    }

    private fun loadShoppingList() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = dao.getAllItems()
            _shoppingList.clear()
            _shoppingList.addAll(items)
        }
    }

    fun addItem(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = ShoppingItem(name = name)
            dao.insertItem(newItem)
            loadShoppingList()
        }
    }

    fun restoreItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertItem(item)
            loadShoppingList()
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch (Dispatchers.IO) {
            dao.deleteItem(item)
            loadShoppingList()
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateItem(item)
            loadShoppingList()
        }
    }

    fun toggleBought(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updateItem = item.copy(isBought = !item.isBought)
            dao.updateItem(updateItem)
            loadShoppingList()
        }
    }
}

class ShoppingListViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
