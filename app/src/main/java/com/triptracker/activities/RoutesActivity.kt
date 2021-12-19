package com.triptracker.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.triptracker.R
import com.triptracker.adaptors.RouteElement
import com.triptracker.adaptors.RoutesAdaptor
import com.triptracker.data.RouteData
import com.triptracker.data.RouteDataDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class RoutesActivity : AppCompatActivity() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private val routesList: MutableList<RouteElement> = arrayListOf<RouteElement>()
    private lateinit var routeDao: RouteDataDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routes_list_activity)
        mRecyclerView = findViewById<RecyclerView>(R.id.routes_list)

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = RoutesAdaptor(routesList) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

        findViewById<FloatingActionButton>(R.id.fab_home).setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            this.startActivity(intent)
        })

        loadRoutes()
    }

    private fun loadRoutes() = runBlocking {
        GlobalScope.launch {
            routeDao = (this@RoutesActivity.application as TripTracker)
                .databaseObj.routeDataDao()
        }
        var routes: List<RouteData> = routeDao.getItems()

        routes.forEach{
            routesList.add(RouteElement(it.title, it.description))
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