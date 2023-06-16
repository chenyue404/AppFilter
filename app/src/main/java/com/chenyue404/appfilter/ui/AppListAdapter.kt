package com.chenyue404.appfilter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.AppListItem

/**
 * Created by cy on 2023/6/13.
 */
class AppListAdapter : RecyclerView.Adapter<AppListAdapter.VH>() {

    val dataList: MutableList<AppListItem> = mutableListOf()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvPackage: TextView = view.findViewById(R.id.tvPackage)
        val tvVersion: TextView = view.findViewById(R.id.tvVersion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_app_list, parent, false)
    )

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = dataList[position]
        holder.ivIcon.setImageDrawable(item.iconDrawable)
        holder.tvLabel.text = item.label
        holder.tvPackage.text = item.packageName
        holder.tvVersion.text = "${item.versionName} (${item.versionCode})"
    }

    fun update(list: List<AppListItem>) {
        DiffUtil.calculateDiff(DiffCallback(dataList, list), false)
            .dispatchUpdatesTo(this)
        dataList.apply {
            clear()
            addAll(list)
        }
    }

    private class DiffCallback(
        val oldList: List<AppListItem>,
        val newList: List<AppListItem>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList.getOrNull(oldItemPosition)?.packageName ==
                    newList.getOrNull(newItemPosition)?.packageName

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = oldList.getOrNull(newItemPosition)
            return oldItem?.packageName == newItem?.packageName
                    && oldItem?.iconDrawable == newItem?.iconDrawable
                    && oldItem?.label == newItem?.label
                    && oldItem?.versionCode == newItem?.versionCode
                    && oldItem?.versionName == newItem?.versionName
        }

    }
}