package com.example.shoppinglist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.dao.ShoppingDao
import com.example.shoppinglist.db.ShoppingDatabase
import com.example.shoppinglist.model.ShoppingItem
import com.example.shoppinglist.model.TabItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModel(application: Application): AndroidViewModel(application) {
    private val dao: ShoppingDao = ShoppingDatabase.getInstance(application).shoppingDao()

    val tabs: StateFlow<List<TabItem>?> = dao.getAllTabs()
        .onEach { fetchedTabs ->
            if (_selectedTabId.value == null && fetchedTabs.isNotEmpty()) {
                _selectedTabId.value = fetchedTabs.first().id
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedTabId = MutableStateFlow<Int?>(null)
    val selectedTabId: StateFlow<Int?> = _selectedTabId

    val shoppingList: StateFlow<List<ShoppingItem>> = _selectedTabId.flatMapLatest { tabId ->
        if (tabId != null) dao.getItemsForTab(tabId) else kotlinx.coroutines.flow.flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tabId: Int) {
        _selectedTabId.value = tabId
    }

    fun addTab(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.insertTab(TabItem(title = title))
            if (_selectedTabId.value == null) {
                _selectedTabId.value = id.toInt()
            }
        }
    }

    fun renameTab(tab: TabItem, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTab(tab.copy(title = newTitle))
        }
    }

    fun deleteTab(tab: TabItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTabWithItems(tab)
            if (_selectedTabId.value == tab.id) {
                _selectedTabId.value = null
            }
        }
    }

    fun addItem(name: String) {
        val currentTabId = _selectedTabId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertItem(ShoppingItem(name = name, tabId = currentTabId))
        }
    }

    fun restoreItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteItem(item)
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateItem(item)
        }
    }

    fun toggleBought(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateItem(item.copy(isBought = !item.isBought))
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
