package uk.co.sullenart.nearlythere

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity(R.layout.activity_main) {

    private lateinit var destinationAdapter: DestinationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        destinationAdapter = DestinationAdapter(this)
        destination_list.adapter = destinationAdapter

        destinationDao.getAllDestinations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { destinations -> destinationAdapter.setAll(destinations) })

        fab.setOnClickListener { view ->
            destinationDao.apply {
                clear()
                addDestination(Destination(name = "First"))
                addDestination(Destination(name = "Second"))
                addDestination(Destination(name = "Third"))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
