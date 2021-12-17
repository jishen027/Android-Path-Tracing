package com.triptracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RouteDataDao {
    @Query("SELECT * from route ORDER by date DESC")
    suspend fun getItems(): List<RouteData>

    @Query("SELECT * from route WHERE id = :id")
    fun getItem(id: Int): RouteData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(singleRouteData: RouteData): Long

    @Update
    suspend fun update(routeData: RouteData)

    @Delete
    suspend fun delete(routeData: RouteData)
}