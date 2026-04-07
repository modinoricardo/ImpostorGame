package com.ricardomodino.impostorgame.data.local.dao

import androidx.room.*
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY source DESC, id ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE source = :source ORDER BY id ASC")
    suspend fun getBySource(source: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("UPDATE categories SET isSelected = :selected WHERE id = :id")
    suspend fun updateSelection(id: Long, selected: Boolean)

    @Query("SELECT COUNT(*) FROM categories WHERE source = 'global'")
    suspend fun countGlobal(): Int

    @Query("SELECT COUNT(*) FROM categories WHERE source = 'seed'")
    suspend fun countSeed(): Int
}
