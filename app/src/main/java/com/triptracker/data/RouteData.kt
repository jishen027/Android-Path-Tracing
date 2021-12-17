package com.triptracker.data
import androidx.room.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "route", indices = [Index(value = ["id","title"])])
data class RouteData(
    @PrimaryKey(autoGenerate = true)var id: Int = 0,
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="description") var description: String? = null) {}