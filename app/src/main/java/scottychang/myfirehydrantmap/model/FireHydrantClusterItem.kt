package scottychang.cafe_walker.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class FireHydrantClusterItem(private val fireHydrant: FireHydrant): ClusterItem {
    override fun getSnippet(): String {
        return fireHydrant.meta ?: ""
    }

    override fun getTitle(): String {
        return fireHydrant.name
    }

    override fun getPosition(): LatLng {
        return LatLng(fireHydrant.latitude!!.toDouble(), fireHydrant.longitude!!.toDouble())
    }

    fun getId(): String {
        return fireHydrant.id
    }
}