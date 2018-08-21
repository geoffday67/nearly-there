package uk.co.sullenart.nearlythere.destination

import uk.co.sullenart.nearlythere.R

class AddDestinationPresenter {
    private lateinit var view: AddDestinationView

    fun bind(view: AddDestinationView) {
        this.view = view
    }
}

interface AddDestinationView {
    fun showName(name: String)
    fun showLatitude(latitude: Double)
    fun showLongitude(longitude: Double)
}
