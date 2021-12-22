package uk.ac.shef.oak.com6510.activities
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
import uk.ac.shef.oak.com6510.data.RouteData
import uk.ac.shef.oak.com6510.data.RouteDataDao
import kotlinx.coroutines.*

class ShowRouteActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var routeDao: RouteDataDao
    lateinit var route: RouteData

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
        }
    }

    private fun getRoute(id: Int) = runBlocking {
        GlobalScope.launch {
            route = routeDao.getItem(id)
            displayData()
        }
    }

    private fun displayData(){
            val titleToolbar = findViewById<Toolbar>(R.id.show_toolbar)
            val descriptionTextView = findViewById<TextView>(R.id.show_route_description)

            titleToolbar.title = route.title
            if(route.description.equals("")) {
                descriptionTextView.text = "<no description>"
            } else {
                descriptionTextView.text = route.description
            }

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
