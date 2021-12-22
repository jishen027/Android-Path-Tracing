package uk.ac.shef.oak.com6510.adaptors

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uk.ac.shef.oak.com6510.R
import uk.ac.shef.oak.com6510.activities.RoutesActivity
import uk.ac.shef.oak.com6510.activities.ShowRouteActivity
import java.util.ArrayList

class RoutesAdaptor : RecyclerView.Adapter<RoutesAdaptor.ViewHolder>, Filterable {
    private lateinit var context: Context
    private var items: MutableList<RouteElement> = ArrayList<RouteElement>()

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

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }
}