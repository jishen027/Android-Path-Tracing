package uk.ac.shef.oak.com6510.data

import android.graphics.Bitmap
import androidx.room.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "image", indices = [Index(value = ["id","title"])])
data class ImageData(
    @PrimaryKey(autoGenerate = true)var id: Int = 0,
    @ColumnInfo(name="routeId") var routeId: Int? = 0,
    @ColumnInfo(name="positionId")var positionId: Int? = 0,
    @ColumnInfo(name="uri") val imageUri: String,
    @ColumnInfo(name="title") var imageTitle: String,
    @ColumnInfo(name="description") var imageDescription: String? = null,
    @ColumnInfo(name="thumbnailUri") var thumbnailUri: String? = null,)
{
    @Ignore
    var thumbnail: Bitmap? = null
}