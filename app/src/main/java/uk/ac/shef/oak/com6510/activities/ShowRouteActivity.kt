package uk.ac.shef.oak.com6510.activities
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.ac.shef.oak.com6510.R
import kotlinx.coroutines.*
import uk.ac.shef.oak.com6510.data.*

class ShowRouteActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var routeDao: RouteDataDao
    private lateinit var positionDao: PositionDataDao
    lateinit var route: RouteData
    lateinit var lastPosition: PositionData

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
        setContentView(R.layout.activity_show_route)
        val bundle: Bundle? = intent.extras
        var routeId = -1

        if (bundle != null) {
            init()
            routeId = bundle.getInt("position")
            getRoute(routeId)
        }
    }

    private fun init() {
        GlobalScope.launch {
            routeDao = (this@ShowRouteActivity.application as TripTracker)
                .databaseObj.routeDataDao()
            positionDao = (this@ShowRouteActivity.application as TripTracker)
                .databaseObj.positionData()
        }
    }

    private fun getRoute(id: Int) = runBlocking {
        GlobalScope.launch {
            route = routeDao.getItem(id)
            var positions = positionDao.getPositionsOfRoute(route.id)
            lastPosition = positions.last()
            displayData()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayData(){
            val titleToolbar = findViewById<Toolbar>(R.id.show_toolbar)
            val descriptionTextView = findViewById<TextView>(R.id.show_route_description)
            val tempTextView = findViewById<TextView>(R.id.temperature_text)
            val pressTextView = findViewById<TextView>(R.id.pressure_text)

            titleToolbar.title = route.title
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
