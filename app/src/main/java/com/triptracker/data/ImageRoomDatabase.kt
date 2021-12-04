package com.triptracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database class with a singleton INSTANCE object.
 */
@Database(entities = [ImageData::class], version = 2, exportSchema = false)
abstract class ImageRoomDatabase: RoomDatabase() {

    abstract fun imageDataDao(): ImageDataDao

    companion object{
        @Volatile
        private var INSTANCE: ImageRoomDatabase? = null
        fun getDatabase(context: Context): ImageRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageRoomDatabase::class.java,
                    "lab5_database"
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