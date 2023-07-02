package com.chenyue404.appfilter.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.util.json.GsonUtil
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.Compare
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Condition
import com.chenyue404.appfilter.entry.DataName
import com.chenyue404.appfilter.entry.DataType
import com.chenyue404.appfilter.entry.SimpleCondition

/**
 * Created by cy on 2023/6/26.
 */
class ConditionListAdapter : RecyclerView.Adapter<ConditionListAdapter.VH>() {
    interface ActionListener {
        fun update(index: Int, condition: Condition) {}
        fun delete(index: Int) {}
    }

    private val dataList: MutableList<Condition> = mutableListOf()

    var actionListener: ActionListener? = null

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val btName: Button? = view.findViewById(R.id.btName)
        val btNot: ToggleButton? = view.findViewById(R.id.btNot)
        val btCompare: Button? = view.findViewById(R.id.btCompare)
        val etData: EditText? = view.findViewById(R.id.etData)
        val ivDelete: ImageView? = view.findViewById(R.id.ivDelete)

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
            holder.btName?.apply {
                text = simpleCondition.name.name
                click {
                    ChooseDataNameDialog.get(it.context, { dataName ->
                        actionListener?.update(
                            holder.bindingAdapterPosition,
                            simpleCondition.apply { name = dataName }
                        )
                    }).show()
                }
            }
            holder.btNot?.apply {
                isChecked = simpleCondition.not == true
                setOnCheckedChangeListener { _, isChecked ->
                    actionListener?.update(
                        holder.bindingAdapterPosition,
                        simpleCondition.apply { not = isChecked }
                    )
                }
            }
            holder.btCompare?.apply {
                text = simpleCondition.compare.name
                click {
                    ChooseCompareDialog.get(it.context, simpleCondition.name.type, { compare ->
                        actionListener?.update(
                            holder.bindingAdapterPosition,
                            simpleCondition.apply { this.compare = compare }
                        )
                    }).show()
                }
            }
            holder.etData?.apply {
                setText(simpleCondition.data.toString())
                setOnEditorActionListener { v, actionId, event ->
                    log("actionId=$actionId, event=$event")
                    true
                }
            }
        } else {
            val compositeCondition = condition as CompositeCondition
            holder.tvId?.text = compositeCondition.list.size.toString()
        }
        holder.ivDelete?.click {
            val clickPosition = dataList.indexOf(condition)
            actionListener?.delete(clickPosition)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        payloads.forEach { payload ->
            val bundle = payload as Bundle
            val condition = dataList[position]
            val combination = bundle.getString("combination")
            val list = bundle.getString("list")
            if (combination != null || list != null) {
                condition as CompositeCondition
                combination?.let {
                    holder.tvId?.text = condition.list.size.toString()
                }
            } else {
                condition as SimpleCondition
                holder.btName?.text = condition.name.name
                holder.btCompare?.text = condition.compare.name
                holder.btNot?.isChecked = condition.not
                holder.etData?.apply {
                    setText(condition.data.toString())
                    setSelection(text.length)
                }
            }
        }
    }

    fun updateList(list: List<Condition>) {
        val diff = DiffUtil.calculateDiff(DiffUtilCallback(dataList, list))
        dataList.clear()
        dataList.addAll(list)
        diff.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
        private val oldList: List<Condition>,
        private val newList: List<Condition>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = newList.getOrNull(newItemPosition)
            val result = (oldItem != null
                    && newItem != null
                    && oldItem.javaClass == newItem.javaClass
                    && oldItem.getUUID() == newItem.getUUID())
            return result
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList.getOrNull(oldItemPosition)
            val newItem = newList.getOrNull(newItemPosition)
            val result = (oldItem != null
                    && newItem != null
                    && oldItem.toString() == newItem.toString())
            return result
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            val bundle = Bundle()
            if (oldItem.not != newItem.not) {
                bundle.putBoolean("not", newItem.not)
            }
            if (oldItem is SimpleCondition) {
                newItem as SimpleCondition
                if (oldItem.name != newItem.name) {
                    bundle.putString("name", newItem.name.name)
                }
                if (oldItem.compare != newItem.compare) {
                    bundle.putString("compare", newItem.compare.name)
                }
                if (oldItem.data != newItem.data) {
                    bundle.putString("data", newItem.data.toString())
                }
            }
            if (oldItem is CompositeCondition) {
                newItem as CompositeCondition
                if (oldItem.combination != newItem.combination) {
                    bundle.putString("combination", newItem.combination.name)
                }
                if (oldItem.list.joinToString() != newItem.list.joinToString()) {
                    bundle.putString("list", GsonUtil.toJson(newItem.list))
                }
            }
            return bundle.takeIf { it.size() > 0 }
        }
    }

    private object ChooseDataNameDialog {
        private var mDialog: AlertDialog? = null
        private val dataNameArray by lazy { DataName.values().map { it.name }.toTypedArray() }
        fun get(
            context: Context,
            chooseListener: ((dataName: DataName) -> Unit)? = null,
            beforeChecked: DataName? = null
        ): AlertDialog {
            val beforeCheckedIndex = beforeChecked?.let { dataNameArray.indexOf(it.name) } ?: -1
            var afterDataName: DataName? = null
            if (mDialog == null) {
                mDialog = AlertDialog.Builder(context)
                    .setSingleChoiceItems(
                        dataNameArray,
                        beforeCheckedIndex
                    ) { _, which ->
                        afterDataName = DataName.valueOf(dataNameArray[which])
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        dialog.cancel()
                        afterDataName?.let {
                            chooseListener?.invoke(it)
                        }
                    }
                    .create()
            }
            beforeCheckedIndex.takeIf { it != -1 }?.let {
                mDialog?.listView?.setItemChecked(it, true)
            }
            return mDialog!!
        }
    }


    private object ChooseCompareDialog {
        private var mDialog: AlertDialog? = null
        fun get(
            context: Context,
            dataType: DataType,
            chooseListener: ((compare: Compare) -> Unit)? = null,
            beforeChecked: Compare? = null
        ): AlertDialog {
            val compareArray = Compare.getMatchArray(dataType)
            val beforeCheckedIndex = beforeChecked?.let { compareArray.indexOf(it) } ?: -1
            var afterCompare: Compare? = null
            if (mDialog == null) {
                mDialog = AlertDialog.Builder(context)
                    .setSingleChoiceItems(
                        compareArray.map { it.name }.toTypedArray(),
                        beforeCheckedIndex
                    ) { _, which ->
                        afterCompare = compareArray[which]
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        dialog.cancel()
                        afterCompare?.let {
                            chooseListener?.invoke(it)
                        }
                    }
                    .create()
            }
            beforeCheckedIndex.takeIf { it != -1 }?.let {
                mDialog?.listView?.setItemChecked(it, true)
            }
            return mDialog!!
        }
    }
}