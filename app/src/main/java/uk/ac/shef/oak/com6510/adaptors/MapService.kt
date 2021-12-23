package uk.ac.shef.oak.com6510.adaptors

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import uk.ac.shef.oak.com6510.activities.MapsActivity
import java.lang.Exception
import java.util.*

class MapService :Service(){
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val TAG = "MapServices"
    private var startMode: Int = 0

    private lateinit var yk: TimerTask;

    private var lastKnownLocation: Location? = null
    private var locationPermissionGranted = false



    override fun onCreate() {
        super.onCreate()

        var CHANNEL_ONE_ID = "uk.ac.shef.aok.com6510"
        var CHANNEL_ONE_NAME = "channel_name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =  NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
            var notification =  Notification.Builder(applicationContext,CHANNEL_ONE_ID).setChannelId(CHANNEL_ONE_ID).build()
            startForeground(1, notification)
        }
        var map = MapsActivity.getMap()
        var activity = MapsActivity.getActivity()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        getLocationPermission()
        updateLocationUI()


        class ykTimer() : TimerTask() {
            override fun run() {
                addPolyLine()
            }
        }
        yk = ykTimer()
        Timer().schedule(yk, Date(), 5000)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand ${intent}")

        return startMode
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        class ykTimer() : TimerTask() {
            override fun run() {
                addPolyLine()
            }
        }
        yk.cancel()
        yk = ykTimer()
        super.onDestroy()
    }



    private fun getLocationPermission(){
        /*
        * Request location permission, so that we can get the location of the
        * device. The result of the permission request is handled by a callback,
        * onRequestPermissionsResult.
        */
        if(ContextCompat.checkSelfPermission(MapsActivity.getActivity()?.applicationContext!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            locationPermissionGranted = true
        }else{
            ActivityCompat.requestPermissions(
                MapsActivity.getActivity()!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun updateLocationUI(){
        if(MapsActivity.getMap() == null){
            return
        }
        try {
            if(locationPermissionGranted){
                MapsActivity.getMap()?.isMyLocationEnabled = true
                MapsActivity.getMap()?.uiSettings?.isMyLocationButtonEnabled = true
            }else{
                MapsActivity.getMap()?.isMyLocationEnabled = false
                MapsActivity.getMap()?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        }catch (e: SecurityException){
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun addPolyLine(){
        Log.e(TAG, "add polyLine is running")
        try {
            if (locationPermissionGranted) {
                Log.e(TAG, "locationPermissionGranted = $locationPermissionGranted")
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(MapsActivity.getActivity()!!) { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, " task is successful")
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            MapsActivity.getActivity()?.runOnUiThread(Runnable {
                                Log.e(TAG, "start doing ployline")
                                val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                                MapsActivity.points.add(markerPosition)
                                MapsActivity.createNewPosition(markerPosition.latitude, markerPosition.longitude)
                                val polylineOptions = PolylineOptions().addAll(MapsActivity.points)
                                var polyline =  MapsActivity.getMap()?.addPolyline(polylineOptions)
                                MapsActivity.polylines.add(polyline!!)
                            })
                        }else{
                            Log.e(TAG, "last known location is empty")
                        }
                    } else {
                        Log.e(TAG, "task exception")

                    }
                }
            }else{
                Log.e(TAG, "locationPermissionGranted = $locationPermissionGranted")
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun cleanMap(){
        for (marker in MapsActivity.markers){
            marker.remove()
        }

        for(polyline in MapsActivity.polylines){
            polyline.remove()
        }
        MapsActivity.markers.removeAll(MapsActivity.markers)
        MapsActivity.points.removeAll(MapsActivity.points)
    }


    companion object {
        private const val DEFAULT_ZOOM = 15
    }
}