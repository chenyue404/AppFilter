package com.chenyue404.appfilter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Condition
import com.chenyue404.appfilter.entry.SimpleCondition

/**
 * Created by cy on 2023/6/26.
 */
class ConditionListAdapter : RecyclerView.Adapter<ConditionListAdapter.VH>() {

    private val dataList: MutableList<Condition> = mutableListOf()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val btName: Button? = view.findViewById(R.id.btName)
        val btNot: ToggleButton? = view.findViewById(R.id.btNot)
        val btCompare: Button? = view.findViewById(R.id.btCompare)
        val etData: EditText? = view.findViewById(R.id.etData)
        val btDelete: Button? = view.findViewById(R.id.btDelete)

        val tvId: TextView? = view.findViewById(R.id.tvId)
        val ivArrow: ImageView? = view.findViewById(R.id.ivArrow)
        val fcv: FragmentContainerView? = view.findViewById(R.id.fcv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                if (viewType == 0) {
                    R.layout.item_simple_condition
                } else {
                    R.layout.item_composite_condition
                }, parent, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is SimpleCondition) {
            0
        } else {
            1
        }
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = getItemViewType(position)
        val condition = dataList[position]
        if (viewType == 0) {
            val simpleCondition = condition as SimpleCondition
            holder.btName?.text = simpleCondition.name.name
            holder.btNot?.isChecked = simpleCondition.not == true
            holder.btCompare?.text = simpleCondition.compare.name
            holder.etData?.setText(simpleCondition.data.toString())
        } else {
            val compositeCondition = condition as CompositeCondition
        }
        holder.btDelete?.click {
            dataList.remove(condition)
            notifyItemRemoved(position)
        }
    }

    fun updateList(list: List<Condition>) {
        DiffUtil.calculateDiff(DiffUtilCallback(dataList, list))
            .dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(list)
    }

    class DiffUtilCallback(
        private val oldList: List<Condition>,
        private val newList: List<Condition>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areContentsTheSame(oldItemPosition, newItemPosition)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = newList.getOrNull(newItemPosition)
            return oldItem != null
                    && newItem != null
                    && oldItem.toString() == newItem.toString()
        }
    }
}