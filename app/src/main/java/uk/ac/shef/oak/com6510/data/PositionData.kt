package uk.ac.shef.oak.com6510.data
import androidx.room.*
import java.util.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "position", indices = [Index(value = ["id"])])
data class PositionData(
    @PrimaryKey(autoGenerate = true)var id: Int = 0,
    @ColumnInfo(name="routeId")var routeId: Int?,
    @ColumnInfo(name="lat") var lat: Double,
    @ColumnInfo(name="lng") var lng: Double,
    @ColumnInfo(name="date") var date: Date,
    @ColumnInfo(name="pressure") var pressure: Float,
    @ColumnInfo(name="temperature") var temperature: Float)