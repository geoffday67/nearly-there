package uk.co.sullenart.nearlythere

import uk.co.sullenart.nearlythere.data.StationDao
import java.io.InputStream
import javax.inject.Inject

class StationManager @Inject constructor(
        val stationDao: StationDao
) {
    fun loadStations(input: InputStream) {
        stationDao.clear()
        stationDao.loadStations(input)
    }

    val stationCount: Int
        get() = stationDao.count()
}