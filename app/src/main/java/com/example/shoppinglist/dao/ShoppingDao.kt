package com.example.shoppinglist.dao

import androidx.room.*
import com.example.shoppinglist.model.ShoppingItem
import com.example.shoppinglist.model.TabItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    // Shopping Items
    @Query("SELECT * FROM shopping_items WHERE tabId = :tabId ORDER BY isBought ASC, id DESC")
    fun getItemsForTab(tabId: Int): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    // Tabs
    @Query("SELECT * FROM tabs")
    fun getAllTabs(): Flow<List<TabItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabItem): Long

    @Delete
    suspend fun deleteTab(tab: TabItem)

    @Query("DELETE FROM shopping_items WHERE tabId = :tabId")
    suspend fun deleteItemsByTab(tabId: Int)

    @Transaction
    suspend fun deleteTabWithItems(tab: TabItem) {
        deleteItemsByTab(tab.id)
        deleteTab(tab)
    }
}
