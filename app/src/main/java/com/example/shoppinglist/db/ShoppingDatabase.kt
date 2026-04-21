package com.example.shoppinglist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shoppinglist.dao.ShoppingDao
import com.example.shoppinglist.model.ShoppingItem
import com.example.shoppinglist.model.TabItem

@Database(entities = [ShoppingItem::class, TabItem::class], version = 5)
abstract class ShoppingDatabase: RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingDatabase? = null

        fun getInstance(context: Context): ShoppingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "shopping_db"
                )
                .fallbackToDestructiveMigration() // For simplicity during dev
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
