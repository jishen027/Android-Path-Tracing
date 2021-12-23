package uk.ac.shef.oak.com6510.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.ac.shef.oak.com6510.R
import uk.ac.shef.oak.com6510.adaptors.RouteElement
import uk.ac.shef.oak.com6510.adaptors.RoutesAdaptor
import uk.ac.shef.oak.com6510.data.RouteData
import uk.ac.shef.oak.com6510.data.RouteDataDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.ArrayList

/**
 * Activity for routes screen. Shows a list of routes with their title and description.
 * There is also a search bar for searching among routes
 */
class RoutesActivity : AppCompatActivity() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private val routesList: MutableList<RouteElement> = arrayListOf<RouteElement>()
    private val allRoutes: MutableList<RouteElement> = arrayListOf<RouteElement>()
    private lateinit var routeDao: RouteDataDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routes_list_activity)
        mRecyclerView = findViewById<RecyclerView>(R.id.routes_list)

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        mAdapter = RoutesAdaptor(this, routesList) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

        val searchView : SearchView = findViewById(R.id.routes_search_view)

        /**
         * set text listener for search view. Filters images based on entered text
         */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                val filteredValue: MutableList<RouteElement> = ArrayList<RouteElement>()
                filteredValue.clear()
                for(each in allRoutes) {
                    if(each.title.lowercase().contains(newText.lowercase()) ||
                        each.description?.lowercase()?.contains(newText.lowercase()) == true
                            ) {
                        filteredValue.add(each)
                    }
                }
                routesList.clear()
                routesList.addAll(filteredValue)
                Log.i("search", routesList.toString())
                mAdapter.notifyDataSetChanged()
                return true
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })

        findViewById<FloatingActionButton>(R.id.fab_home).setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            this.startActivity(intent)
        })
        init()
    }

    /**
     * for navigation to another screen with passing params
     */
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val pos = result.data?.getIntExtra("position", -1)!!
                val id = result.data?.getIntExtra("id", -1)!!
                val del_flag = result.data?.getIntExtra("deletion_flag", -1)!!
                if (pos != -1 && id != -1) {
                    if (result.resultCode == Activity.RESULT_OK) {
                        when(del_flag){
                            -1, 0 -> mAdapter.notifyDataSetChanged()
                            else -> mAdapter.notifyItemRemoved(pos)
                        }
                    }
                }
            }
        }

    /**
     * Loads DAO object
     */
    private fun init() {
        GlobalScope.launch {
            routeDao = (this@RoutesActivity.application as TripTracker)
                .databaseObj.routeDataDao()

            loadRoutes()
        }
    }

    /**
     * Loads all routes from database and stores it in two local variables
     * one for keeping all data and the other for search result
     *
     */
    private fun loadRoutes() = runBlocking {
        var routes: List<RouteData> = routeDao.getItems()

        routes.forEach{
            routesList.add(RouteElement(it.title, it.description, it.id))
            allRoutes.add(RouteElement(it.title, it.description, it.id))
        }
    }

}