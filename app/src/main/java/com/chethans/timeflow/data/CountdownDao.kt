package com.chethans.timeflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {

    @Insert
    suspend fun insert(countdown: CountdownEntity): Long

    @Insert
    suspend fun insertAll(countdowns: List<CountdownEntity>): List<Long>

    @Delete
    suspend fun delete(countdown: CountdownEntity)

    @Update
    suspend fun update(countdown: CountdownEntity)

    @Query("SELECT * FROM countdowns ORDER BY targetTime ASC")
    fun getAll(): Flow<List<CountdownEntity>>

    @Query("SELECT * FROM countdowns ORDER BY targetTime ASC")
    suspend fun getAllList(): List<CountdownEntity>

    @Query("SELECT * FROM countdowns ORDER BY targetTime ASC LIMIT 1")
    suspend fun getNextCountdown(): CountdownEntity?

    @Query("SELECT * FROM countdowns WHERE id = :id")
    suspend fun getCountdownById(id: Int): CountdownEntity?

    @Query("SELECT * FROM countdowns WHERE id IN (:ids)")
    suspend fun getCountdownsByIds(ids: List<Int>): List<CountdownEntity>

    @Query("UPDATE countdowns SET isArchived = :archived WHERE id IN (:ids)")
    suspend fun updateArchived(ids: List<Int>, archived: Boolean)

    @Query("UPDATE countdowns SET category = :category WHERE id IN (:ids)")
    suspend fun updateCategory(ids: List<Int>, category: String)

    @Query("DELETE FROM countdowns WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("DELETE FROM countdowns")
    suspend fun clearAll()
}