package scottychang.cafe_walker.adapter

import androidx.recyclerview.widget.DiffUtil
import scottychang.cafe_walker.model.FireHydrant

class FireHydrantDiffCallback(
    private val oldList: List<Pair<FireHydrant, Double>>?,
    private val newList: List<Pair<FireHydrant, Double>>?
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList?.size ?: 0

    override fun getNewListSize(): Int = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)?.first
        val newItem = newList?.get(newItemPosition)?.first
        return oldItem?.equals(newItem) ?: false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)
        val newItem = newList?.get(newItemPosition)
        return oldItem?.equals(newItem) ?: false
    }
}