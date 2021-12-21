package com.triptracker.activities

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
import com.triptracker.R
import com.triptracker.adaptors.RouteElement
import com.triptracker.adaptors.RoutesAdaptor
import com.triptracker.data.ImageData
import com.triptracker.data.RouteData
import com.triptracker.data.RouteDataDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.ArrayList


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
        loadRoutes()
    }

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

    private fun init() {
        GlobalScope.launch {
            routeDao = (this@RoutesActivity.application as TripTracker)
                .databaseObj.routeDataDao()
        }
    }

    private fun loadRoutes() = runBlocking {
        var routes: List<RouteData> = routeDao.getItems()

        routes.forEach{
            routesList.add(RouteElement(it.title, it.description, it.id))
            allRoutes.add(RouteElement(it.title, it.description, it.id))
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        val id = item.itemId
//        return if (id == R.id.action_settings) {
//            true
//        } else super.onOptionsItemSelected(item)
//    }


}