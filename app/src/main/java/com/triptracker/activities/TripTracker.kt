package com.triptracker.activities

import android.app.Application
import com.triptracker.data.TripTrackerRoomDatabase

class TripTracker: Application() {
    val databaseObj: TripTrackerRoomDatabase by lazy { TripTrackerRoomDatabase.getDatabase(this) }
}
