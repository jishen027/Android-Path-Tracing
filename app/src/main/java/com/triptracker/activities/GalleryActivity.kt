package com.triptracker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.triptracker.adaptors.GalleryAdapter
import com.triptracker.data.ImageData
import com.triptracker.data.ImageDataDao
import kotlinx.coroutines.*
import pl.aprilapps.easyphotopicker.*
import java.util.ArrayList
import com.triptracker.R

class GalleryActivity : AppCompatActivity() {
    private var myDataset: MutableList<ImageData> = ArrayList<ImageData>()
    private lateinit var daoObj: ImageDataDao
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var easyImage: EasyImage
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        val ADAPTER_ITEM_DELETED = 100
        private const val REQUEST_READ_EXTERNAL_STORAGE = 2987
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 7829
        private const val REQUEST_CAMERA_CODE = 100

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        initData()
        // Log.d("TAG", "message")
        mRecyclerView = findViewById(R.id.grid_recycler_view)
        // set up the RecyclerView
        val numberOfColumns = 4
        mRecyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)
        mAdapter = GalleryAdapter(myDataset) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

        // required by Android 6.0 +
        checkPermissions(applicationContext)
        initEasyImage()

        // the floating button that will allow us to get the images from the Gallery
        val fabGallery: FloatingActionButton = findViewById(R.id.fab_gallery)
        fabGallery.setOnClickListener(View.OnClickListener {
            easyImage.openChooser(this@GalleryActivity)
        })
    }

    /**
     * it initialises EasyImage
     */
    private fun initEasyImage() {
        easyImage = EasyImage.Builder(this)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .allowMultiple(true)
            .build()
    }

    private fun initData() {
        GlobalScope.launch {
            daoObj = (this@GalleryActivity.application as TripTracker)
                .databaseObj.imageDataDao()
            myDataset.addAll(daoObj.getItems())
        }
    }

    /**
     * check permissions are necessary starting from Android 6
     * if you do not set the permissions, the activity will simply not work and you will be probably baffled for some hours
     * until you find a note on StackOverflow
     * @param context the calling context
     */
    private fun checkPermissions(context: Context) {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder: AlertDialog.Builder =
                        AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("External storage permission is necessary")
                    alertBuilder.setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.requestPermissions(
                                context as Activity, arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ), REQUEST_READ_EXTERNAL_STORAGE
                            )
                        })
                    val alert: AlertDialog = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE
                    )
                }
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("Writing external storage permission is necessary")
                    alertBuilder.setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.requestPermissions(
                                context as Activity, arrayOf(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ), REQUEST_WRITE_EXTERNAL_STORAGE
                            )
                        })
                    val alert: AlertDialog = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_STORAGE
                    )
                }
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_CODE);
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        easyImage.handleActivityResult(requestCode, resultCode,data,this,
            object: DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    onPhotosReturned(imageFiles)
                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {
                    super.onImagePickerError(error, source)
                }

                override fun onCanceled(source: MediaSource) {
                    super.onCanceled(source)
                }
            })
    }

    /**
     * add the selected images to the grid
     * @param returnedPhotos
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        myDataset.addAll(getImageData(returnedPhotos))

        // we tell the adapter that the data is changed and hence the grid needs
        mAdapter.notifyDataSetChanged()
        mRecyclerView.scrollToPosition(returnedPhotos.size - 1)
    }

    /**
     * insert a ImageData into the database
     * Called for each image the user adds by clicking the fab button
     * Then retrieves the same image so we can have the automatically assigned id field
     */
    private fun insertData(imageData: ImageData): Int = runBlocking {
        var insertJob = async { daoObj.insert(imageData) }
        insertJob.await().toInt()
    }

    /**
     * given a list of photos, it creates a list of ImageData objects
     * we do not know how many elements we will have
     * @param returnedPhotos
     * @return
     */
    private fun getImageData(returnedPhotos: Array<MediaFile>): List<ImageData> {
        val imageDataList: MutableList<ImageData> = ArrayList<ImageData>()
        for (mediaFile in returnedPhotos) {
            val fileNameAsTempTitle = mediaFile.file.name
            var imageData = ImageData(
                imageTitle = fileNameAsTempTitle,
                imageUri = mediaFile.file.absolutePath
            )
            // Update the database with the newly created object
//            var id = insertData(imageData)
            var id = insertData(imageData)
            imageData.id = id
            imageDataList.add(imageData)
        }
        return imageDataList
    }
}