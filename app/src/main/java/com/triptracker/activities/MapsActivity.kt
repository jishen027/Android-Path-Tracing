package com.triptracker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.triptracker.R
import com.triptracker.data.*
import com.triptracker.databinding.ActivityMapsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.aprilapps.easyphotopicker.EasyImage
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var startRouteDialog: Dialog
    private lateinit var startRecordBtn: Button;
    private lateinit var yk: TimerTask;

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // The Location Permission
    private var locationPermissionGranted = false

    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)

    private var cameraPosition: CameraPosition? = null

    private var buttonState = true
    private var currentRouteId: Int = -1;
    private var currentPressure: Float = 0f;
    private var currentTemperature: Float = 0f;

    //marker and polyline
    var points = mutableListOf<LatLng>()
    var markers = mutableListOf<Marker>()
    var polylines = mutableListOf<Polyline>()



    // sensor
    private var sensorViewModel: SensorViewModel? = null

    //dao
    private lateinit var positionDao: PositionDataDao
    private lateinit var routeDao: RouteDataDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Sensor activities
        this.sensorViewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        this.sensorViewModel!!.retrievePressureData()!!.observe(this,
            //  create observer, whenever the value is changed this func will be called
            { newValue ->
                newValue?.also{
                    Log.i("Data in UI - Pressure", it.toString())
                    currentPressure = it;
                }
            })

        this.sensorViewModel!!.retrieveTemperatureData()!!.observe(this,
            { newValue ->
                newValue?.also {
                    Log.i("Data in UI - Temp", it.toString())
                    currentTemperature = it;
                }
            }
        )

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        // Activities for buttons =============================================================

        /**
         * ykTimer is for set a regular intervals(5 seconds) to track user's location temperature and pressure.
         * */
        class ykTimer() : TimerTask() {
            override fun run() {
                addPolyLine()
            }
        }
        yk = ykTimer()
        startRecordBtn = findViewById<Button>(R.id.start_record)
        startRecordBtn.setOnClickListener(){
            if(buttonState) {
                cleanMap()
                yk = ykTimer()
                openStartRouteDialog()
            } else {
                this.sensorViewModel?.stopSensing()
                yk.cancel()
                yk = ykTimer()
                addMarkerPoint()
                setAddPhotoVisible()
                startRecordBtn.text = resources.getString(R.string.start);
            }
            buttonState = !buttonState
        }

        findViewById<FloatingActionButton>(R.id.gallery_btn).setOnClickListener(View.OnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            this.startActivity(intent)
        })

        findViewById<FloatingActionButton>(R.id.routes_btn).setOnClickListener(View.OnClickListener {
            val intent = Intent(this, RoutesActivity::class.java)
            this.startActivity(intent)
        })

        findViewById<FloatingActionButton>(R.id.addPhotoFab).setOnClickListener(View.OnClickListener {

        })

        initData();

        //end of  activities for buttons =============================================================
    }

    private fun openStartRouteDialog() {
        startRouteDialog = Dialog(this)
        startRouteDialog.setContentView(R.layout.new_route_popup)
        val cancelBtn = startRouteDialog.findViewById<Button>(R.id.cancel_routing_btn)
        val startBtn = startRouteDialog.findViewById<Button>(R.id.start_routing_btn)
        val routeTitle = startRouteDialog.findViewById<EditText>(R.id.route_title_field)
        val routeDesc = startRouteDialog.findViewById<EditText>(R.id.route_title_desc)

        startBtn.setOnClickListener {
            Timer().schedule(yk, Date(), 5000)
            addMarkerPoint()
            this.sensorViewModel?.startSensing()
            setAddPhotoVisible()
            startRecordBtn.text = resources.getString(R.string.stop)
            createNewRoute(routeTitle.text.toString(), routeDesc.text.toString())
            startRouteDialog.dismiss()
            checkData()
        }
        cancelBtn.setOnClickListener {
            startRouteDialog.dismiss()
        }
        startRouteDialog.show()
    }

    private fun initData() {
        GlobalScope.launch {
            routeDao = (this@MapsActivity.application as TripTracker)
                .databaseObj.routeDataDao()
            positionDao = (this@MapsActivity.application as TripTracker)
                .databaseObj.positionData()
        }
    }

    private fun checkData() = runBlocking {
        var routes: List<RouteData> = routeDao.getItems();
        Log.i("positions", routes.toString())
    }

    private fun createNewRoute(title: String, desc: String) {
        Log.i("new route", title)
        Log.i("new route", desc)
        var routeData = RouteData(title=title, description=desc, date = Date())
        var id = insertRoute(routeData)
        currentRouteId = id
    }

    private fun insertRoute(routeDate: RouteData): Int = runBlocking {
        var insertJob = async { routeDao.insert(routeDate) }
        insertJob.await().toInt()
    }

    private fun createNewPosition(lat: Double, lng: Double) {
        var positionData = PositionData(routeId=currentRouteId, lat = lat, lng= lng, date= Date(),
            pressure = currentPressure, temperature = currentTemperature)
        insertPosition(positionData)
        Log.i("positions", currentPressure.toString())
        Log.i("positions", currentTemperature.toString())
    }

    private fun insertPosition(positionData: PositionData): Int = runBlocking {
        var insertJob = async { positionDao.insert(positionData) }
        insertJob.await().toInt()
    }
    /**
     * addMarkerPoint() will get user's current position(latitude and longitude) and put a marker on the map
     *
     * */
    private fun addMarkerPoint(){
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                                var marker = mMap?.addMarker(
                                    MarkerOptions()
                                        .position(markerPosition)
                                )
                            points.add(markerPosition)
                            createNewPosition(markerPosition.latitude, markerPosition.longitude)
                            if (marker != null) {
                                markers.add(marker)
                            }
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    /***
     * get current position and insert the position to the latlng list then draw a ployline on the map
     */
    private fun addPolyLine(){
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            points.add(markerPosition)
                            createNewPosition(markerPosition.latitude, markerPosition.longitude)
                            val polylineOptions = PolylineOptions().addAll(points)
                            var polyline =  mMap.addPolyline(polylineOptions)
                            polylines.add(polyline)
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    /**
     * remove all markers and polyline on the map
     */
    private fun cleanMap(){
        for (marker in markers){
            marker.remove()
        }

        for(polyline in polylines){
            polyline.remove()
        }
        markers.removeAll(markers)
        points.removeAll(points)
    }

    /**
     * user's current position(latitude and longitude) and put a image marker on the map
     */
    private fun addImageMarker(){
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val markerPosition = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.jetpack)
                            mMap?.addMarker(
                                MarkerOptions()
                                    .position(markerPosition)
                                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(
                                        android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, false)))
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.mMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                    findViewById<FrameLayout>(R.id.map), false)
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })

        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

    }

    private fun getLocationPermission(){
        /*
        * Request location permission, so that we can get the location of the
        * device. The result of the permission request is handled by a callback,
        * onRequestPermissionsResult.
        */
        if(ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            locationPermissionGranted = true
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when(requestCode){
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI(){
        if(mMap == null){
            return
        }
        try {
            if(locationPermissionGranted){
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            }else{
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        }catch (e: SecurityException){
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(){
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            showCurrentPlace()
        }
        return true
    }

    private fun setAddPhotoVisible() {
        if (findViewById<FloatingActionButton>(R.id.addPhotoFab).visibility == View.VISIBLE) {
            findViewById<FloatingActionButton>(R.id.addPhotoFab).visibility = View.INVISIBLE
        } else {
            findViewById<FloatingActionButton>(R.id.addPhotoFab).visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (mMap == null) {
            return
        }
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count = if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                        likelyPlaces.placeLikelihoods.size
                    } else {
                        M_MAX_ENTRIES
                    }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLngs = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                        i++
                        if (i > count - 1) {
                            break
                        }
                    }

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.")

            // Add a default marker, because the user hasn't selected a place.
            mMap?.addMarker(MarkerOptions()
                .title(getString(R.string.default_info_title))
                .position(defaultLocation)
                .snippet(getString(R.string.default_info_snippet)))

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    private fun openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        val listener = DialogInterface.OnClickListener { dialog, which -> // The "which" argument contains the position of the selected item.
            val markerLatLng = likelyPlaceLatLngs[which]
            var markerSnippet = likelyPlaceAddresses[which]
            if (likelyPlaceAttributions[which] != null) {
                markerSnippet = """
                $markerSnippet
                ${likelyPlaceAttributions[which]}
                """.trimIndent()
            }

            if (markerLatLng == null) {
                return@OnClickListener
            }

            // Add a marker for the selected place, with an info window
            // showing information about that place.
            mMap?.addMarker(MarkerOptions()
                .title(likelyPlaceNames[which])
                .position(markerLatLng)
                .snippet(markerSnippet))

            // Position the map's camera at the location of the marker.
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                DEFAULT_ZOOM.toFloat()))
        }

        // Display the dialog.
        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mMap?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }
}