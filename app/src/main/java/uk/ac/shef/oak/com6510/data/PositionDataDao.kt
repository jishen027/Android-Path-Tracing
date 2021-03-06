package uk.ac.shef.oak.com6510.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Database access object to access the Positions Data using database
 */
@Dao
interface PositionDataDao {
    @Query("SELECT * from position ORDER by id ASC")
    suspend fun getItems(): List<PositionData>

    @Query("SELECT * from position WHERE id = :id")
    fun getItem(id: Int): PositionData

    @Query("SELECT * from position WHERE routeId = :routeId")
    fun getPositionsOfRoute(routeId: Int): List<PositionData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(singlePositionData: PositionData): Long

    @Update
    suspend fun update(positionData: PositionData)

    @Delete
    suspend fun delete(positionData: PositionData)
}