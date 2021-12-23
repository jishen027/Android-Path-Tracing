package uk.ac.shef.oak.com6510.activities
import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.ac.shef.oak.com6510.R
import kotlinx.coroutines.*
import uk.ac.shef.oak.com6510.adaptors.GalleryAdapter
import uk.ac.shef.oak.com6510.data.*
import android.provider.MediaStore

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.R.attr.text


/**
 * Activity of showing details of the route, containing title, description, pressure and
 * temperature of the place of route and an image of that route (if exists)
 */
class ShowRouteActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var routeDao: RouteDataDao
    private lateinit var positionDao: PositionDataDao
    private lateinit var imageDao: ImageDataDao
    lateinit var route: RouteData
    lateinit var lastPosition: PositionData
    private var routeImage: String = ""

    /**
     * for navigation to another screen with passing params
     */
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val position = result.data?.getIntExtra("position", -1)
            val id = result.data?.getIntExtra("id", -1)
            val del_flag = result.data?.getIntExtra("deletion_flag", -1)
            var intent = Intent().putExtra("position", position)
                .putExtra("id", id).putExtra("deletion_flag", del_flag)
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    this.setResult(result.resultCode, intent)
                    this.finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_route)
        val bundle: Bundle? = intent.extras
        var routeId = -1

        if (bundle != null) {
            init()
            routeId = bundle.getInt("position")
            getRoute(routeId)
        }
    }

    /**
     * loads DAO objects
     */
    private fun init() {
        GlobalScope.launch {
            routeDao = (this@ShowRouteActivity.application as TripTracker)
                .databaseObj.routeDataDao()
            positionDao = (this@ShowRouteActivity.application as TripTracker)
                .databaseObj.positionData()
            imageDao = (this@ShowRouteActivity.application as TripTracker)
                .databaseObj.imageDataDao()
        }
    }

    /**
     *  loads data of the route from database
     */
    private fun getRoute(id: Int) = runBlocking {
        GlobalScope.launch {
            route = routeDao.getItem(id)
            var positions = positionDao.getPositionsOfRoute(route.id)
            lastPosition = positions.last()
            displayData()
            loadImage()
        }
    }

    /**
     *  sets first image of the route as a background
     */
    private fun setImage() {
        runOnUiThread(Runnable {
            if(routeImage != "") {
                val imageView = findViewById<ImageView>(R.id.route_image)
                val bitmap = BitmapFactory.decodeFile(routeImage).also { bitmap -> imageView.setImageBitmap(bitmap) }
                imageView.setImageBitmap(bitmap)
            }
        })
    }

    /**
     *  loads image of the route
     */

    private fun loadImage() = runBlocking {
        GlobalScope.launch {
            var images = imageDao.getRouteImages(route.id)
            if(images.isNotEmpty()) {
                routeImage = images[0].imageUri
                setImage()
            }
        }
    }

    /**
     *  assigns data of the route to components of the screen
     */
    private fun displayData(){
            val titleToolbar = findViewById<Toolbar>(R.id.show_toolbar)
            val descriptionTextView = findViewById<TextView>(R.id.show_route_description)
            val tempTextView = findViewById<TextView>(R.id.temperature_text)
            val pressTextView = findViewById<TextView>(R.id.pressure_text)

            titleToolbar.title = "Route: ${route.title}"
            if(route.description.equals("")) {
                descriptionTextView.text = "<no description>"
            } else {
                descriptionTextView.text = route.description
            }

            tempTextView.text = "Temperature: ${lastPosition.temperature} ℃"
            pressTextView.text = "Pressure: ${lastPosition.pressure} (hPa)"

            val fabEdit: FloatingActionButton = findViewById(R.id.fab_edit)
            fabEdit.setOnClickListener(View.OnClickListener {
                startForResult.launch(
                    Intent( this, EditRouteActivity::class.java).apply {
                        putExtra("position", route.id)
                    }
                )
            })

            val fabReturn: FloatingActionButton = findViewById(R.id.return_fab)
            fabReturn.setOnClickListener(View.OnClickListener {
                startForResult.launch(
                    Intent( this, RoutesActivity::class.java).apply {}
                )
            })
    }
}
