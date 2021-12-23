package uk.ac.shef.oak.com6510.adaptors

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
        var map = MapsActivity.getMap()
        var activity = MapsActivity.getActivity()
        getLocationPermission()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)

        Log.e(TAG,"onCreate ${map.toString()}")
        Log.e(TAG,"onCreate ${activity.toString()}")


    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand ${intent.data}")
        class ykTimer() : TimerTask() {
            override fun run() {
                addPolyLine()
            }
        }
        yk = ykTimer()
        Timer().schedule(yk, Date(), 5000)
        return startMode
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        cleanMap()

        super.onDestroy()
    }

//
//
//    private fun getDeviceLocation(intent: Intent?){
//        if(LocationResult.hasResult(intent!!)){
//            val locationResult = LocationResult.extractResult(intent)
//            for(location in locationResult.locations){
//                if(location == null) continue
//
//                Log.i("this is in service", "current location: $location")
//                lastKnownLocation = location
//
//                if(MapsActivity.getActivity() != null){
//                    Log.e(TAG, "got ployline")
//                    MapsActivity.getActivity()?.runOnUiThread(Runnable {
//                        try {
//                            if(lastKnownLocation!=null){
//
//                                val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
//                                MapsActivity.getMap()?.moveCamera(
//                                    CameraUpdateFactory.newLatLngZoom(
//                                        LatLng(lastKnownLocation!!.latitude,
//                                            lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
//
//                                MapsActivity.getMap()?.addMarker(
//                                    MarkerOptions()
//                                        .position(markerPosition)
//                                )
//                            }else{
//                                Log.e(TAG, "last know location is empty")
//                            }
//
//                        }catch (e: Exception){
//                            Log.e("MapServices", "error cannot write on map"+e.message)
//                        }
//                    })
//                }else{
//                    Log.e(TAG, "getActivity is empty")
//                }
//            }
//        }
//    }

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

//    private fun updateLocationUI(){
//        if(MapsActivity.getMap() == null){
//            return
//        }
//        try {
//            if(locationPermissionGranted){
//                MapsActivity.getMap()?.isMyLocationEnabled = true
//                MapsActivity.getMap()?.uiSettings?.isMyLocationButtonEnabled = true
//            }else{
//                MapsActivity.getMap()?.isMyLocationEnabled = false
//                MapsActivity.getMap()?.uiSettings?.isMyLocationButtonEnabled = false
//                lastKnownLocation = null
//                getLocationPermission()
//            }
//        }catch (e: SecurityException){
//            Log.e("Exception: %s", e.message, e)
//        }
//    }

    private fun addPolyLine(){
        Log.e(TAG, "add polyLine is running")
        try {
            if (locationPermissionGranted) {
                Log.e(TAG, "locationPermissionGranted = $locationPermissionGranted")
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(MapsActivity.getActivity()!!) { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, " task is successful")
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            Log.e(TAG, "start doing ployline")
                            val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            MapsActivity.points.add(markerPosition)

//                            createNewPosition(markerPosition.latitude, markerPosition.longitude)
                            val polylineOptions = PolylineOptions().addAll(MapsActivity.points)
                            var polyline =  MapsActivity.getMap()?.addPolyline(polylineOptions)
                            MapsActivity.polylines.add(polyline!!)
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



//    private fun addPolyLine(intent: Intent){
//
//        Log.i(TAG, "add polyLine is running:  ${intent.data}")
//        if(LocationResult.hasResult(intent)){
//            val locationResult = LocationResult.extractResult(intent)
//            for(location in locationResult.locations){
//                if(location == null) continue
//
//                Log.i("this is in service", "current location: $location")
//                lastKnownLocation = location
//
//                if(MapsActivity.getActivity() != null){
//                    MapsActivity.getActivity()?.runOnUiThread(Runnable {
//                        try {
//                            if(lastKnownLocation!=null){
//                                val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
//                                points.add(markerPosition)
//                                val polylineOptions = PolylineOptions().addAll(points)
//                                if(MapsActivity.getMap() != null){
//                                    Log.e(TAG, "google map is not empty")
//                                    var polyline = MapsActivity.getMap()?.addPolyline(polylineOptions)
//                                    polylines.add(polyline!!)
//                                }else{
//                                    Log.e(TAG, "google map is empty")
//                                }
//
//                            }else{
//                                Log.e(TAG, "last known location is empty")
//                            }
//
//                        }catch (e: Exception){
//                            Log.e("MapServices", "error cannot write on map"+e.message)
//                        }
//                    })
//                }else{
//                    Log.e(TAG, "google activity is empty")
//                }
//            }
//        }else{
//            Log.e(TAG, "location result is empty")
//        }
//    }

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