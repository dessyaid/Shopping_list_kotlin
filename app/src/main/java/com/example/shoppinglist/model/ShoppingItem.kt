package com.example.shoppinglist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    val name: String,
    var isBought: Boolean = false,
    val tabId: Int, // Link to TabItem
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
