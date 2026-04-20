package com.example.shoppinglist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shoppinglist.dao.ShoppingDao
import com.example.shoppinglist.db.ShoppingDatabase
import com.example.shoppinglist.model.ShoppingItem
import com.example.shoppinglist.model.TabItem
import kotlinx.coroutines.flow.first
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
    private var testTabId: Int = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShoppingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.shoppingDao()
        
        testTabId = dao.insertTab(TabItem(title = "Test Tab")).toInt()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false, tabId = testTabId)
        dao.insertItem(item)

        val allItems = dao.getItemsForTab(testTabId).first()
        assertTrue(allItems.any { it.name == "TestItem" })
    }

    @Test
    fun updateItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false, tabId = testTabId)
        dao.insertItem(item)

        val itemFromDb = dao.getItemsForTab(testTabId).first().find { it.name == "TestItem" }
        assertNotNull(itemFromDb)

        val newName = "NewTestItem"
        dao.updateItem(itemFromDb!!.copy(name = newName))

        val allItems = dao.getItemsForTab(testTabId).first()
        assertTrue(allItems.any { it.name == newName })
    }

    @Test
    fun deleteItemTest() = runBlocking {
        val item = ShoppingItem(name = "TestItem", isBought = false, tabId = testTabId)
        dao.insertItem(item)

        val itemFromDb = dao.getItemsForTab(testTabId).first().find { it.name == "TestItem" }
        assertNotNull(itemFromDb)

        dao.deleteItem(itemFromDb!!)

        val allItems = dao.getItemsForTab(testTabId).first()
        assertFalse(allItems.any { it.name == "TestItem" })
    }

    @Test
    fun deleteTabWithItemsTest() = runBlocking {
        val item = ShoppingItem(name = "InTab", isBought = false, tabId = testTabId)
        dao.insertItem(item)
        
        val tab = dao.getAllTabs().first().first { it.id == testTabId }
        dao.deleteTabWithItems(tab)
        
        val allTabs = dao.getAllTabs().first()
        assertFalse(allTabs.any { it.id == testTabId })
        
        val allItems = dao.getItemsForTab(testTabId).first()
        assertTrue(allItems.isEmpty())
    }
}
