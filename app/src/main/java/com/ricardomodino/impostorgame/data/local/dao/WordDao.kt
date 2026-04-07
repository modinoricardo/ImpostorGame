package com.ricardomodino.impostorgame.data.local.dao

import androidx.room.*
import com.ricardomodino.impostorgame.data.local.entities.WordEntity

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE categoryId = :categoryId ORDER BY position ASC")
    suspend fun getByCategory(categoryId: Long): List<WordEntity>

    @Query("SELECT * FROM words WHERE categoryId = :categoryId AND position = :position LIMIT 1")
    suspend fun getByPosition(categoryId: Long, position: Int): WordEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>): List<Long>

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("DELETE FROM words WHERE categoryId = :categoryId AND source = 'local'")
    suspend fun deleteLocalByCategory(categoryId: Long)

    @Query("UPDATE words SET nameEn = :nameEn, hintsEn = :hintsEn WHERE categoryId = :categoryId AND position = :position")
    suspend fun updateEnglish(categoryId: Long, position: Int, nameEn: String, hintsEn: String)

    @Query("UPDATE words SET nameZhHans = :name, hintsZhHans = :hints WHERE categoryId = :categoryId AND position = :position")
    suspend fun updateZhHans(categoryId: Long, position: Int, name: String, hints: String)

    @Query("UPDATE words SET nameZhHant = :name, hintsZhHant = :hints WHERE categoryId = :categoryId AND position = :position")
    suspend fun updateZhHant(categoryId: Long, position: Int, name: String, hints: String)
}
