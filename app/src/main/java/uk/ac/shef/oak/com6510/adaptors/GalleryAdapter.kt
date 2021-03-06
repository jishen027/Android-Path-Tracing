package uk.ac.shef.oak.com6510.adaptors

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com6510.R
import uk.ac.shef.oak.com6510.data.ImageData
import uk.ac.shef.oak.com6510.activities.GalleryActivity
import uk.ac.shef.oak.com6510.activities.ShowImageActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.ViewHolder>  {
    private lateinit var context: Context
    lateinit var tempImageData: MutableList<ImageData>

    constructor(cont: Context, items: List<ImageData>) : super() {
        Companion.items = items as MutableList<ImageData>
        context = cont
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_image,
            parent, false
        )
        val holder: ViewHolder = ViewHolder(v)
        context = parent.context
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].thumbnail == null) {
            holder.imageTitle.text = items[position].imageTitle
            items[position].let {
                scope.launch {
                    val bitmap =
                        decodeSampledBitmapFromResource(it.imageUri, 150, 150)
                    bitmap?.let {
                        items[position].thumbnail = it
                        holder.imageView.setImageBitmap(items[position].thumbnail)
                    }
                }
            }
        } else {
            holder.imageTitle.text = items[position].imageTitle
            holder.imageView.setImageBitmap(items[position].thumbnail)
        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            val galleryActivityContext = context as GalleryActivity
            galleryActivityContext.startForResult.launch(
                Intent(context, ShowImageActivity::class.java).apply {
                    putExtra("position", position)
                }
            )
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById<View>(R.id.image_item) as ImageView
        var imageTitle: TextView = itemView.findViewById<View>(R.id.img_title) as TextView

    }

    companion object {
        lateinit var items: MutableList<ImageData>
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        /**
         * helper function to generate a bitmap object of a given size from an image's file path.
         */
        suspend fun decodeSampledBitmapFromResource(
            filePath: String,
            reqWidth: Int,
            reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()

            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(filePath, options);
        }

        /**
         * helper function to calculate an inSampleSize for resampling to achieve the right picture
         * density for a smaller size file.
         * See inSampleFile: https://developer.android.com/reference/android/graphics/BitmapFactory.Options#inSampleSize
         */
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight;
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = (height / 2).toInt()
                val halfWidth = (width / 2).toInt()

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth
                ) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize.toInt();
        }
    }
}