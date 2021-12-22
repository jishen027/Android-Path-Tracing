package uk.ac.shef.oak.com6510.activities
import android.app.Activity
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.os.Bundle
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

class ShowImageActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lateinit var daoObj: ImageDataDao

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

        daoObj = (this@ShowImageActivity.application as TripTracker)
            .databaseObj.imageDataDao()

        if (bundle != null) {
            // this is the image position in the itemList
            position = bundle.getInt("position")
            displayData(position)
        }
    }

    private fun displayData(position: Int){
        if (position != -1) {
            val imageView = findViewById<ImageView>(R.id.show_image)
            val titleToolbar = findViewById<Toolbar>(R.id.show_toolbar)
            val descriptionTextView = findViewById<TextView>(R.id.show_image_description)
            val imageData = GalleryAdapter.items[position]

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
