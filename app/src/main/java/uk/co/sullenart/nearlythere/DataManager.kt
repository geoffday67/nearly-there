package uk.co.sullenart.nearlythere

import io.reactivex.Flowable
import uk.co.sullenart.nearlythere.data.DestinationDao
import uk.co.sullenart.nearlythere.model.Destination

class DataManager (val destinationDao: DestinationDao){
    fun getAllDestinations(): Flowable<List<Destination>> = destinationDao.getAllDestinations()

    fun addDestination(destination: Destination) = destinationDao.addDestination(destination)

    fun deleteDestination(destination: Destination) {
        destinationDao.delete(destination.id)
    }

    fun setDestinationActive(destination: Destination, active: Boolean) {
        destinationDao.setActive(destination.id, active)
    }
}