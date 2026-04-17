package com.example.shoppinglist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shoppinglist.dao.ShoppingDao
import com.example.shoppinglist.db.ShoppingDatabase
import com.example.shoppinglist.model.ShoppingItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {
    private lateinit var db: ShoppingDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShoppingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.shoppingDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false)
        dao.insertItem(item)

        val allItems = dao.getAllItems()
        assertTrue(allItems.any { it.name == "TestItem" })
    }

    @Test
    fun updateItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false)

        dao.insertItem(item)

        val itemFromDb = dao.getAllItems().find { it.name == "TestItem" }
        assertNotNull(itemFromDb)

        val newName = "NewTestItem"
        dao.updateItem(itemFromDb!!.copy(name = newName))

        val allItems = dao.getAllItems()
        assertTrue(allItems.any { it.name == newName })

    }

    @Test
    fun deleteItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false)
        dao.insertItem(item)

        val itemFromDb = dao.getAllItems().find { it.name == "TestItem" }
        assertNotNull(itemFromDb)

        dao.deleteItem(itemFromDb!!)

        val allItems = dao.getAllItems()
        assertFalse(allItems.any { it.name == "TestItem" })
    }


}
