package com.triptracker.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.triptracker.R
import com.triptracker.data.RouteData
import com.triptracker.data.RouteDataDao
import kotlinx.coroutines.*

class EditRouteActivity : AppCompatActivity() {

    private lateinit var routeDao: RouteDataDao
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lateinit var route: RouteData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_route)

        val bundle: Bundle? = intent.extras
        var routeId = -1

        if (bundle != null) {
            init()
            routeId = bundle.getInt("position")
            getRoute(routeId)
            makeButtonListeners()
            loadData()
        }

    }

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

    private fun init() {
        GlobalScope.launch {
            routeDao = (this@EditRouteActivity.application as TripTracker)
                .databaseObj.routeDataDao()
        }
    }

    private fun getRoute(id: Int) = runBlocking {
        GlobalScope.launch {
            route = routeDao.getItem(id)
        }
    }

    private fun loadData() {
        val editRouteTitle = findViewById<EditText>(R.id.edit_route_title)
        val editRouteDesc = findViewById<EditText>(R.id.edit_route_description)
        editRouteTitle.setText(route.title)
        editRouteDesc.setText(route.description)
    }

    private fun makeButtonListeners() {
        var id = route.id
        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            this@EditRouteActivity.finish()
        }

        // Delete button listener
        val deleteButton: Button = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            scope.launch(Dispatchers.IO) {
                async { routeDao.delete(route) }
            }

            startForResult.launch(
                Intent( this, RoutesActivity::class.java).apply {}
            )
        }

        // Save button listener
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val descriptionTextInput =
                findViewById<EditText>(R.id.edit_route_description)
            route.description = descriptionTextInput.text.toString()
            val titleTextInput = findViewById<EditText>(R.id.edit_route_title)
            route.title = titleTextInput.text.toString()

            scope.launch(Dispatchers.IO) {
                async { routeDao.update(route) }
                this@EditRouteActivity.finish()
            }

            startForResult.launch(
                Intent( this, ShowRouteActivity::class.java).apply {
                    putExtra("position", route.id)
                }
            )
        }
    }
}