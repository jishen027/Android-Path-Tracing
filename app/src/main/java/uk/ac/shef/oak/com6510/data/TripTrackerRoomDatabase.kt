package uk.ac.shef.oak.com6510.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Database class with a singleton INSTANCE object.
 */
@Database(entities = [ImageData::class, PositionData::class, RouteData::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TripTrackerRoomDatabase: RoomDatabase() {

    abstract fun imageDataDao(): ImageDataDao
    abstract fun routeDataDao(): RouteDataDao
    abstract fun positionData(): PositionDataDao


    companion object{
        @Volatile
        private var INSTANCE: TripTrackerRoomDatabase? = null
        fun getDatabase(context: Context): TripTrackerRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripTrackerRoomDatabase::class.java,
                    "trip_tracker_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object specified.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                return instance
            }
        }
    }
}