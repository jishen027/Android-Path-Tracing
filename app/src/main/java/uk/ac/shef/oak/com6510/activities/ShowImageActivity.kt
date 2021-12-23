package uk.ac.shef.oak.com6510.activities
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
import uk.ac.shef.oak.com6510.adaptors.GalleryAdapter
import uk.ac.shef.oak.com6510.data.ImageDataDao
import kotlinx.coroutines.*
import uk.ac.shef.oak.com6510.data.PositionData
import uk.ac.shef.oak.com6510.data.PositionDataDao
import uk.ac.shef.oak.com6510.data.RouteDataDao

class ShowImageActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var positionDao: PositionDataDao
    private lateinit var routeDao: RouteDataDao
    var position: PositionData? = null

    val startForResult =
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
        setContentView(R.layout.activity_show_image)
        val bundle: Bundle? = intent.extras
        var position = -1

        if (bundle != null) {
            // this is the image position in the itemList
            position = bundle.getInt("position")
            loadPosition(position)
            loadRoute(position)
            displayData(position)
        }
    }

    private fun loadRoute(position: Int) {
        GlobalScope.launch {
            routeDao = (this@ShowImageActivity.application as TripTracker)
                .databaseObj.routeDataDao()
            var routeId = GalleryAdapter.items[position].routeId
            if(routeId != null && routeId > 0) {
                var route = routeDao.getItem(routeId)
                val routeNameTextView = findViewById<TextView>(R.id.img_route_name)
                routeNameTextView.text = "Route: ${route?.title}"
            }

        }
    }

    private fun loadPosition(position: Int) {
        GlobalScope.launch {
            positionDao = (this@ShowImageActivity.application as TripTracker)
                .databaseObj.positionData()
            if(GalleryAdapter.items[position].positionId != null) {
                var positionId = GalleryAdapter.items[position].positionId
                this@ShowImageActivity.position = positionId?.let { positionDao.getItem(it) }
                displayPosition()
            } else {
                val posContainer = findViewById<View>(R.id.barometer_container)
                posContainer.visibility = View.INVISIBLE
            }
        }
    }

    private fun displayPosition() {
        var classPosition = this@ShowImageActivity.position
        val tempTextView = findViewById<TextView>(R.id.img_temperature_text)
        val pressTextView = findViewById<TextView>(R.id.img_pressure_text)
        if(classPosition?.temperature == null) {
            val posContainer = findViewById<View>(R.id.barometer_container)
            posContainer.visibility = View.INVISIBLE
        } else {
            tempTextView.text = "Temperature: ${classPosition?.temperature} ℃"
            pressTextView.text = "Pressure: ${classPosition?.pressure} (hPa)"
        }
    }

    private fun displayData(position: Int){
        if (position != -1) {
            val imageView = findViewById<ImageView>(R.id.show_image)
            val titleToolbar = findViewById<Toolbar>(R.id.show_toolbar)
            val descriptionTextView = findViewById<TextView>(R.id.show_image_description)

            imageView.setImageBitmap(GalleryAdapter.items[position].thumbnail!!)
            titleToolbar.title = GalleryAdapter.items[position].imageTitle
            if(GalleryAdapter.items[position].imageDescription.equals("")) {
                descriptionTextView.text = "<no description>"
            } else {
                descriptionTextView.text =  GalleryAdapter.items[position].imageDescription
            }

            val fabEdit: FloatingActionButton = findViewById(R.id.fab_edit)
            fabEdit.setOnClickListener(View.OnClickListener {
                startForResult.launch(
                    Intent( this, EditImageActivity::class.java).apply {
                        putExtra("position", position)
                    }
                )
            })

            val fabReturn: FloatingActionButton = findViewById(R.id.return_fab)
            fabReturn.setOnClickListener(View.OnClickListener {
                startForResult.launch(
                    Intent( this, GalleryActivity::class.java).apply {}
                )
            })
        }
    }
}
