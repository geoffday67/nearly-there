package uk.co.sullenart.nearlythere

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import uk.co.sullenart.nearlythere.model.Destination

class DestinationAdapter(context: Context) : ArrayAdapter<Destination>(context, R.layout.destination_list_item) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val destination = getItem(position)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.destination_list_item, parent, false)
        view.findViewById<TextView>(R.id.destination_name).text = destination.name
        view.findViewById<TextView>(R.id.destination_state).text =
                if (destination.active) "Active" else "Inactive"

        return view
    }

    fun setAll(collection: Collection<Destination>) {
        clear()
        addAll(collection)
    }
}