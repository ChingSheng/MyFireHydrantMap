package scottychang.cafe_walker.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import scottychang.cafe_walker.data.ZoneString
import scottychang.cafe_walker.model.FireHydrant
import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.model.TwZone
import scottychang.cafe_walker.repositiory.FireHydrantRepository
import scottychang.cafe_walker.repositiory.MyCallback
import scottychang.cafe_walker.repositiory.SharePrefRepository
import java.util.*
import kotlin.collections.HashMap

class FireHydrantViewModel(application: Application) : AndroidViewModel(application) {
    private val MAX_SHOPS_IN_BOTTOM_SHEET = 50

    var fireHydrantShops = MutableLiveData<List<FireHydrant>>()
    var exceptions = MutableLiveData<Exception>()
    var loading = MutableLiveData<Boolean>()

    var currentCity: TwZone = TwZone.UNKNOWN
        private set
    var current: Map<String, FireHydrant> = HashMap()
        private set

    init {
        var zone = SharePrefRepository.getInstance().loadZone(getApplication())
        if (zone == TwZone.UNKNOWN) zone = getNearestZone(application)
        setFireHydrant(zone)
    }

    fun setFireHydrant(twZone: TwZone) {
        this.currentCity = twZone
        SharePrefRepository.getInstance().saveCity(getApplication(), twZone)
        loading.postValue(true)
        val zone = getApplication<Application>().resources.getString(ZoneString.data[twZone]!!)
        val iis  =getApplication<Application>().assets.open(zone + ".csv")
        Log.d("DADA", "" + iis.available())
        FireHydrantRepository.getInstance(getApplication()).loadFireHydrant(iis, object :
            MyCallback<List<FireHydrant>> {
            override fun onFailure(exception: Exception) {
                loading.postValue(false)
                exceptions.postValue(exception)
                fireHydrantShops.postValue(Collections.emptyList())
            }

            override fun onSuccess(result: List<FireHydrant>) {
                loading.postValue(false)
                current = result.associate { it.id to it }.toMap()
                fireHydrantShops.postValue(result)
            }
        })
    }

//    fun setCoffeeShopsCity(twZone: TwZone) {
//        this.currentCity = twZone
//        SharePrefRepository.getInstance().saveCity(getApplication(), twZone)
//
//        loading.postValue(true)
//        FireHydrantRepository.getInstance(getApplication())
//            .loadCoffeeShops(twZone.type, object :
//                MyCallback<List<FireHydrant>> {
//                override fun onFailure(exception: Exception) {
//                    loading.postValue(false)
//                    exceptions.postValue(exception)
//                    fireHydrantShops.postValue(Collections.emptyList())
//                }
//
//                override fun onSuccess(result: List<FireHydrant>) {
//                    loading.postValue(false)
//                    current = result.associate { it.id to it }.toMap()
//                    fireHydrantShops.postValue(result)
//                }
//            })
//    }

    fun getDistancePairFromPosition(position: LatLng): List<Pair<FireHydrant, Double>> {
        val fireHydrantDistancePair = current.map { item ->
            Pair(
                item.value,
                getDistance(position, getLatLng(item.value))
            )
        }
        val result = fireHydrantDistancePair.toList().sortedBy { (_, distance) -> distance }.subList(
            0,
            Math.min(MAX_SHOPS_IN_BOTTOM_SHEET, fireHydrantDistancePair.size)
        )
        return result
    }

    private fun getLatLng(fireHydrant: FireHydrant): LatLng {
        return LatLng(fireHydrant.latitude?.toDouble() ?: .0, fireHydrant.longitude?.toDouble() ?: .0)
    }

    fun getNearestZone(context: Context): TwZone {
//        val current = PositioningRepository.loadLatLng(context)
//        var bestDistance = -.1
//        var city: TwCity = TwZone.UNKNOWN
//        for ((twCity, latlng) in CityLatLng.data) {
//            val distance = getDistance(latlng, current)
//            if (bestDistance < 0 || distance < bestDistance) {
//                bestDistance = distance
//                city = twCity
//            }
//        }
        return TwZone.SUNG_SHAN
    }

    private fun getDistance(point1: LatLng, point2: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, result)
        return result.get(0).toDouble()
    }
}