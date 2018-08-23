package uk.co.sullenart.nearlythere.destination

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.location.places.ui.PlacePicker
import uk.co.sullenart.nearlythere.BaseActivity
import uk.co.sullenart.nearlythere.R
import uk.co.sullenart.nearlythere.model.Destination

class AddDestinationActivity : BaseActivity(R.layout.activity_add_destination) {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AddDestinationActivity::class.java))
        }

        private const val PLACE_REQUEST_CODE = 1
    }

    @BindView(R.id.destination_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.name_layout)
    lateinit var nameLayout: TextInputLayout

    @BindView(R.id.latitude_layout)
    lateinit var latitudeLayout: TextInputLayout

    @BindView(R.id.longitude_layout)
    lateinit var longitudeLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.add_destination_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_destination, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.destination_save -> {
                    saveDestination(); true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun saveDestination() {
        val name = nameLayout.editText?.text.toString().apply {
            if (isEmpty()) {
                nameLayout.error = "Enter a valid name"
                return
            }
        }

        val latitude = latitudeLayout.editText?.text.toString().toDoubleOrNull()
        if (latitude == null) {
            latitudeLayout.error = "Enter a valid latitude"
            return
        }

        val longitude = longitudeLayout.editText?.text.toString().toDoubleOrNull()
        if (longitude == null) {
            longitudeLayout.error = "Enter a valid longitude"
            return
        }

        val destination = Destination(name, latitude, longitude, true)
        dataManager.addDestination(destination)

        finish()
    }

    @OnClick(R.id.destination_search)
    fun onSearch() {
        val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                .setFilter(
                        AutocompleteFilter.Builder()
                                // TODO Choose whether to filter by country
                                .setCountry("UK")
                                .build()
                )
                .build(this)
        //val intent = PlacePicker.IntentBuilder().build(this)

        startActivityForResult(intent, PLACE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_REQUEST_CODE && resultCode == RESULT_OK) {
            PlaceAutocomplete.getPlace(this, data).let {
            //PlacePicker.getPlace(this, data).let {
                nameLayout.editText?.setText (it.name.toString())
                latitudeLayout.editText?.setText(it.latLng.latitude.toString())
                longitudeLayout.editText?.setText(it.latLng.longitude.toString())
            }
        }
    }
}
