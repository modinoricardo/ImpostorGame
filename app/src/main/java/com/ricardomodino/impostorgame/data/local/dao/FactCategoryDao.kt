package com.ricardomodino.impostorgame.data.local.dao

import androidx.room.*
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity

@Dao
interface FactCategoryDao {
    @Query("SELECT * FROM fact_categories ORDER BY source DESC, id ASC")
    suspend fun getAll(): List<FactCategoryEntity>

    @Query("SELECT * FROM fact_categories WHERE id = :id")
    suspend fun getById(id: Long): FactCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: FactCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<FactCategoryEntity>): List<Long>

    @Update
    suspend fun update(category: FactCategoryEntity)

    @Delete
    suspend fun delete(category: FactCategoryEntity)

    @Query("UPDATE fact_categories SET isSelected = :selected WHERE id = :id")
    suspend fun updateSelection(id: Long, selected: Boolean)

    @Query("SELECT COUNT(*) FROM fact_categories WHERE source = 'global'")
    suspend fun countGlobal(): Int
}
