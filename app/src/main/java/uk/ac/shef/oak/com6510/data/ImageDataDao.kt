package uk.ac.shef.oak.com6510.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Image Data using database
 */
@Dao
interface ImageDataDao {
    @Query("SELECT * from image ORDER by id ASC")
    suspend fun getItems(): List<ImageData>

    @Query("SELECT * from image WHERE id = :id")
    fun getItem(id: Int): ImageData

    @Query("SELECT * from image WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<ImageData>>

    @Query("SELECT * from image WHERE routeId = :routeId")
    fun getRouteImages(routeId: Int): List<ImageData>
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