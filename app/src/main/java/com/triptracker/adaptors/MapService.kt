package com.triptracker.adaptors

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.triptracker.activities.MapsActivity
import java.lang.Exception
import java.util.*
import java.util.jar.Manifest

class MapService :Service(){
    private val TAG = "MapServices"
    private var startMode: Int = 0

    private var lastKnownLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

        Log.e(TAG,"onCreate")
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        getDeviceLocation(intent)

        return startMode
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun getDeviceLocation(intent: Intent?){
        if(LocationResult.hasResult(intent!!)){
            val locationResult = LocationResult.extractResult(intent)
            for(location in locationResult.locations){
                if(location == null) continue

                Log.i("this is in service", "current location: $location")
                lastKnownLocation = location

                if(MapsActivity.getActivity() != null){
                    MapsActivity.getActivity()?.runOnUiThread(Runnable {
                        try {

                            if(lastKnownLocation!=null){
                                val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                                MapsActivity.getMap()?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))

                                MapsActivity.getMap()?.addMarker(
                                    MarkerOptions()
                                        .position(markerPosition)
                                )
                            }

                        }catch (e: Exception){
                            Log.e("MapServices", "error cannot write on map"+e.message)
                        }
                    })
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
    }
}