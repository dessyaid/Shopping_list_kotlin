package com.example.shoppinglist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListTheme {
                ShoppingListScreen()
            }
        }
    }
}

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    val name: String,
    var isBought: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY isBought ASC, id DESC")
    fun getAllItems(): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: ShoppingItem)

    @Update
    fun updateItem(item: ShoppingItem)

    @Delete
    fun deleteItem(item: ShoppingItem)
}

@Database(entities = [ShoppingItem::class], version = 1)
abstract class ShoppingDatabase: RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingDatabase? = null

        fun getInstance(context: Context): ShoppingDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "shopping_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

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
