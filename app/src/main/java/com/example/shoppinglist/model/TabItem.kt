package com.example.shoppinglist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String
)
