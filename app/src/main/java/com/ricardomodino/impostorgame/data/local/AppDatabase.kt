package com.ricardomodino.impostorgame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ricardomodino.impostorgame.data.local.dao.*
import com.ricardomodino.impostorgame.data.local.entities.*

@Database(
    entities = [
        CategoryEntity::class,
        WordEntity::class,
        FactCategoryEntity::class,
        FactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun wordDao(): WordDao
    abstract fun factCategoryDao(): FactCategoryDao
    abstract fun factDao(): FactDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "impostor_game_db"
                ).build().also { INSTANCE = it }
            }
    }
}
