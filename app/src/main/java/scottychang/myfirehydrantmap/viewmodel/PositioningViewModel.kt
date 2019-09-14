package scottychang.cafe_walker.viewmodel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.repositiory.PositioningRepository

class PositioningViewModel(application: Application) : AndroidViewModel(application) {
    var latLng = MutableLiveData<LatLng>()
        private set

    init {
        reloadPosition()
    }

    fun reloadPosition() {
        val positioning = PositioningRepository.loadLatLng(getApplication())
        latLng.postValue(positioning)
    }
}