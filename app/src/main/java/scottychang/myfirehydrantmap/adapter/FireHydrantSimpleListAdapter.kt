package scottychang.cafe_walker.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import scottychang.cafe_walker.model.FireHydrant
import scottychang.myfirehydrantmap.R
import scottychang.myfirehydrantmap.adapter.viewholder.FireHydrantViewHolder
import java.lang.ref.WeakReference

class FireHydrantSimpleListAdapter(
    private var data: List<Pair<FireHydrant, Double>>?,
    private val onItemClick: (id: String?) -> Unit = {},
    private val onItemLongClick: (id: String?) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var referenceRecyclerView: WeakReference<RecyclerView> = WeakReference<RecyclerView>(null)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        referenceRecyclerView = WeakReference(recyclerView)
    }

    fun updateData(newData: List<Pair<FireHydrant, Double>>?) {
        val result = DiffUtil.calculateDiff(FireHydrantDiffCallback(data, newData))
        data = newData
        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_fire_hydrant_simple_item, parent, false)
        return FireHydrantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onItemClick.invoke(data?.get(position)?.first?.id) }
        holder.itemView.setOnLongClickListener {onItemLongClick.invoke(data?.get(position)?.first?.id)
            true}
        (holder as FireHydrantViewHolder).onBind(data?.get(position)!!)
    }
}