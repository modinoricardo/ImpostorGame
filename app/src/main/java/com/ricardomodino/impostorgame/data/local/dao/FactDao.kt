package com.ricardomodino.impostorgame.data.local.dao

import androidx.room.*
import com.ricardomodino.impostorgame.data.local.entities.FactEntity

@Dao
interface FactDao {
    @Query("SELECT * FROM facts WHERE factCategoryId = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<FactEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fact: FactEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(facts: List<FactEntity>): List<Long>

    @Update
    suspend fun update(fact: FactEntity)

    @Delete
    suspend fun delete(fact: FactEntity)
}
