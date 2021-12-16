package com.triptracker.data
import androidx.room.*
import java.util.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "position", indices = [Index(value = ["id"])])
data class PositionData(
    @PrimaryKey(autoGenerate = true)var id: Int = 0,
    @ColumnInfo(name="routeId")var positionId: Int?,
    @ColumnInfo(name="lat") var lat: Float,
    @ColumnInfo(name="lng") var lng: Float,
    @ColumnInfo(name="date") var date: Date,
    @ColumnInfo(name="pressure") var pressure: Float)