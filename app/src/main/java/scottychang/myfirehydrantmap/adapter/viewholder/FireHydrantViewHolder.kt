package scottychang.myfirehydrantmap.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import scottychang.cafe_walker.model.FireHydrant
import scottychang.myfirehydrantmap.R

class FireHydrantViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    fun onBind(fireHydrant: Pair<FireHydrant, Double>) {
        itemView.findViewById<TextView>(R.id.name).text = fireHydrant.first.name
        itemView.findViewById<TextView>(R.id.distance).text = setDistance(fireHydrant.second)
        itemView.findViewById<TextView>(R.id.shop_metadata_simple).text = fireHydrant.first.meta
        setMetaData(fireHydrant)
    }

    private fun setMetaData(fireHydrant: Pair<FireHydrant, Double>) {
        val metadata = itemView.findViewById<TextView>(R.id.shop_metadata_simple)
        metadata.visibility = if (metadata.text.length > 0) View.VISIBLE else View.GONE
    }

    private fun setDistance(second: Double): String =
        if (second < 1000) {
            second.toInt().toString() + "m"
        } else {
            String.format("%.1f", second / 1000) + "km"
        }
}