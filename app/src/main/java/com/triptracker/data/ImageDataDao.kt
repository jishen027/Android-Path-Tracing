package com.triptracker.data

import android.app.Person

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.triptracker.data.ImageData
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface ImageDataDao {
    @Query("SELECT * from image ORDER by id ASC")
    suspend fun getItems(): List<ImageData>

    @Query("SELECT * from image WHERE id = :id")
    fun getItem(id: Int): ImageData

    @Query("SELECT * from image WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<ImageData>>

    // Specify the conflict strategy as REPLACE,
    // when the trying to add an existing Item
    // into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(singleImageData: ImageData): Long

    @Update
    suspend fun update(imageData: ImageData)

    @Delete
    suspend fun delete(imageData: ImageData)
}