package uk.co.sullenart.nearlythere

import android.content.Context
import android.widget.ArrayAdapter

class DestinationAdapter(context: Context) : ArrayAdapter<Destination>(context, R.layout.destination_list_item, R.id.destination_name) {
    fun setAll(collection: Collection<Destination>) {
        clear()
        addAll(collection)
    }
}