package uk.co.sullenart.nearlythere

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import uk.co.sullenart.nearlythere.model.Subject

interface SubjectMenuListener {
    fun onMenu (subject: Subject, view: View)
}

class SubjectAdapter(context: Context, val listener: SubjectMenuListener) : ArrayAdapter<Subject>(context, R.layout.destination_list_item) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val subject = getItem(position)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.destination_list_item, parent, false)
        view.findViewById<TextView>(R.id.destination_name).apply {
            text = subject.destination.name
            if (!subject.destination.active) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
        view.findViewById<ImageButton>(R.id.destination_menu).setOnClickListener {
            listener.onMenu(subject, it)
        }

        if (subject.highlighted) {
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

    fun setHighlightByName(name: String, highlight: Boolean) {
        for (i in 0 until count) {
            val item = getItem(i)
            if (item.destination.name == name) {
                item.highlighted = highlight
                notifyDataSetChanged()
                return
            }
        }
    }

    fun setAll(collection: Collection<Subject>) {
        clear()
        addAll(collection)
    }
}