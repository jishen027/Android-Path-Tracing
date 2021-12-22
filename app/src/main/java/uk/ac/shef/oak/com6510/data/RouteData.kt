package uk.ac.shef.oak.com6510.data
import androidx.room.*
import java.util.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "route", indices = [Index(value = ["id","title"])])
data class RouteData(
    @PrimaryKey(autoGenerate = true)var id: Int = 0,
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="description") var description: String? = null,
    @ColumnInfo(name="date") var date: Date? = null)