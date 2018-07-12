package uk.co.sullenart.nearlythere

import android.content.Context
import android.content.Intent
import android.os.Bundle

class AddDestinationActivity : BaseActivity(R.layout.activity_add_destination) {
    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, AddDestinationActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
