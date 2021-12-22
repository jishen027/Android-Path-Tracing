package uk.ac.shef.oak.com6510.activities

import android.app.Application
import uk.ac.shef.oak.com6510.data.TripTrackerRoomDatabase

class TripTracker: Application() {
    val databaseObj: TripTrackerRoomDatabase by lazy { TripTrackerRoomDatabase.getDatabase(this) }
}
