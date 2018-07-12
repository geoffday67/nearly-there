package uk.co.sullenart.nearlythere

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import uk.co.sullenart.nearlythere.model.Subject

class SubjectAdapter(context: Context) : ArrayAdapter<Subject>(context, R.layout.destination_list_item) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val subject = getItem(position)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.destination_list_item, parent, false)
        view.findViewById<TextView>(R.id.destination_name).text = subject.destination.name
        view.findViewById<TextView>(R.id.destination_state).text =
                if (subject.destination.active) "Active" else "Inactive"

        if (subject.nearby) {
            view.setBackgroundColor(Color.CYAN)
        }

        return view
    }

    fun update(subject: Subject) {
        for (i in 0 until count) {
            val item = getItem(i)
            if (item.destination.name == subject.destination.name) {
                remove(item)
                insert(subject, i)
                return
            }
        }
    }

    fun setAll(collection: Collection<Subject>) {
        clear()
        addAll(collection)
    }
}