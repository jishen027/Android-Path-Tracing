package com.triptracker.adaptors

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.triptracker.R
import com.triptracker.activities.GalleryActivity
import com.triptracker.activities.RoutesActivity
import com.triptracker.activities.ShowImageActivity
import com.triptracker.activities.ShowRouteActivity

class RoutesAdaptor : RecyclerView.Adapter<RoutesAdaptor.ViewHolder> {
    private lateinit var context: Context
    var items: MutableList<RouteElement>

    constructor(cont: Context, items: MutableList<RouteElement>) {
        this.items = items
        context = cont
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.route_item_activity,
            parent, false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position] != null) {
            holder.title.text = items[position].title
            holder.description.text = items[position].description
        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            val routesActivityContext = context as RoutesActivity
            routesActivityContext.startForResult.launch(
                Intent(context, ShowRouteActivity::class.java).apply {
                    putExtra("position", items[position].id)
                }
            )
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    public class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var image: Image
        var title: TextView = itemView.findViewById<View>(R.id.route_list_title) as TextView
        var description: TextView = itemView.findViewById<View>(R.id.route_list_description) as TextView

    }
}