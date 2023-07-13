package com.chenyue404.appfilter.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.util.KeyboardUtil
import com.chenyue404.androidlib.util.json.GsonUtil
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.Combination
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

    companion object {
        private const val key_not = "not"
        private const val key_name = "name"
        private const val key_compare = "compare"
        private const val key_data = "data"
        private const val key_combination = "combination"
        private const val key_list = "list"
        private const val type_simple = 0
        private const val type_composite = 1
    }

    interface ActionListener {
        fun update(index: Int, condition: Condition)
        fun delete(index: Int)
    }

    private val dataList: MutableList<Condition> = mutableListOf()

    var actionListener: ActionListener? = null
    var compositeConditionActivityLauncher: ((intent: Intent) -> Unit)? = null

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val btName: Button? = view.findViewById(R.id.btName)
        val btNot: ToggleButton? = view.findViewById(R.id.btNot)
        val btCompare: Button? = view.findViewById(R.id.btCompare)
        val etData: EditText? = view.findViewById(R.id.etData)
        val ivDelete: ImageView? = view.findViewById(R.id.ivDelete)

        val tvId: TextView? = view.findViewById(R.id.tvId)
        val ivArrow: ImageView? = view.findViewById(R.id.ivArrow)
        val fcv: FragmentContainerView? = view.findViewById(R.id.fcv)

        fun initClick(
            dataList: MutableList<Condition>,
            onDeleteItem: (position: Int) -> Unit,
            onUpdateItem: (position: Int, payload: Bundle?) -> Unit,
            compositeConditionActivityLauncher: ((intent: Intent) -> Unit)? = null
        ) {
            ivDelete?.click {
                onDeleteItem(bindingAdapterPosition)
            }
            btName?.click {
                ChooseDataNameDialog.get(
                    it.context, { dataName ->
                        onUpdateItem(
                            bindingAdapterPosition, Bundle().apply {
                                putString(key_name, dataName.name)
                            }
                        )
                    },
                    (dataList[bindingAdapterPosition] as SimpleCondition).name
                ).show()
            }
            btNot?.setOnCheckedChangeListener { buttonView, isChecked ->
                onUpdateItem(bindingAdapterPosition, Bundle().apply {
                    putBoolean(key_not, isChecked)
                })
            }
            btCompare?.click {
                val position = bindingAdapterPosition
                ChooseCompareDialog.get(
                    it.context,
                    (dataList[position] as SimpleCondition).name.type, { compare ->
                        onUpdateItem(position, Bundle().apply {
                            putString(key_compare, compare.name)
                        })
                    },
                    (dataList[position] as SimpleCondition).compare
                ).show()
            }
            etData?.setOnEditorActionListener { v, actionId, event ->
                var result = false
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    result = true
                    KeyboardUtil.hideSoftInput(v)
                    onUpdateItem(bindingAdapterPosition, Bundle().apply {
                        putString(key_data, (v as EditText).text.toString())
                    })
                }
                result
            }
            itemView.click {
                if (itemViewType == type_simple) return@click
                val intent = Intent(it.context, CompositeConditionActivity::class.java).apply {
                    putExtra(
                        CompositeConditionActivity.extra_key_composite_condition,
                        GsonUtil.toJson(dataList[bindingAdapterPosition])
                    )
                    putExtra(
                        CompositeConditionActivity.extra_key_from_position,
                        bindingAdapterPosition
                    )
                }
                compositeConditionActivityLauncher?.invoke(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                if (viewType == type_simple) {
                    R.layout.item_simple_condition
                } else {
                    R.layout.item_composite_condition
                }, parent, false
            )
        ).apply {
            initClick(
                dataList,
                onDeleteItem = {
                    actionListener?.delete(it)
                    dataList.removeAt(it)
                    notifyItemRemoved(it)
                },
                onUpdateItem = { position, payload ->
                    val newCondition = updateConditionByPayload(dataList[position], payload)
                    payload?.let {
                        this@ConditionListAdapter.onBindViewHolder(
                            this,
                            position,
                            mutableListOf(payload)
                        )
                    }
                    actionListener?.update(position, newCondition)
                },
                compositeConditionActivityLauncher
            )

        }
    }

    private fun updateConditionByPayload(condition: Condition, payload: Bundle?): Condition {
        payload ?: return condition
        payload.get(key_not)?.let {
            condition.not = it as Boolean
        }
        val combination = payload.getString(key_combination)
        val list = payload.getString(key_list)
        if (combination != null || list != null) {
            condition as CompositeCondition
            combination?.let {
                condition.combination = Combination.valueOf(it)
            }
            list?.let {
                condition.list = GsonUtil.listFromJson<Condition>(list).toMutableList()
            }
        } else {
            condition as SimpleCondition
            payload.getString(key_name)?.let {
                condition.name = DataName.valueOf(it)
            }
            payload.getString(key_compare)?.let {
                condition.compare = Compare.valueOf(it)
            }
            payload.get(key_data)?.let {
                condition.data = it
            }
        }
        return condition
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is SimpleCondition) {
            type_simple
        } else {
            type_composite
        }
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val viewType = getItemViewType(position)
        val condition = dataList[position]
        if (viewType == type_simple) {
            condition as SimpleCondition
            holder.btName?.text = condition.name.name
            holder.btNot?.isChecked = condition.not == true
            holder.btCompare?.text = condition.compare.name
            holder.etData?.apply {
                setText(condition.data.toString())
                setSelection(text.length)
                hint = condition.name.type.getHintText(context)
            }
        } else {
            condition as CompositeCondition
            holder.tvId?.text = condition.list.size.toString()
        }
        holder.ivDelete?.click {
            val clickPosition = holder.bindingAdapterPosition
            actionListener?.delete(clickPosition)
            dataList.removeAt(clickPosition)
            notifyItemRemoved(clickPosition)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        val itemType = getItemViewType(position)
        payloads.forEach { payload ->
            val bundle = payload as Bundle
            if (itemType == type_composite) {
                bundle.getString(key_list)?.let {
                    holder.tvId?.text = GsonUtil.listFromJson<Condition>(it).size.toString()
                }
            } else {
                payload.getString(key_name)?.let {
                    holder.btName?.text = it
                    holder.etData?.hint =
                        DataName.valueOf(it).type.getHintText(holder.itemView.context)
                }
                payload.getString(key_compare)?.let {
                    holder.btCompare?.text = it
                }
                payload.get(key_data)?.let {
                    holder.etData?.apply {
                        setText(it.toString())
                        setSelection(text.length)
                    }
                }
                payload.get(key_not)?.let {
                    holder.btNot?.isChecked = it == true
                }
            }
        }
    }

    fun updateList(list: List<Condition>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun addItem(condition: Condition) {
        dataList.add(condition)
        notifyItemInserted(dataList.size - 1)
    }

    fun updateItem(condition: Condition, position: Int) {
        dataList[position] = condition
        notifyItemChanged(position)
    }

    private object ChooseDataNameDialog {
        private val dataNameArray by lazy { DataName.values().map { it.name }.toTypedArray() }
        fun get(
            context: Context,
            chooseListener: ((dataName: DataName) -> Unit)? = null,
            beforeChecked: DataName? = null
        ): AlertDialog {
            val beforeCheckedIndex = beforeChecked?.let { dataNameArray.indexOf(it.name) } ?: -1
            var afterDataName: DataName? = null
            return AlertDialog.Builder(context)
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
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
        }
    }


    private object ChooseCompareDialog {
        fun get(
            context: Context,
            dataType: DataType,
            chooseListener: ((compare: Compare) -> Unit)? = null,
            beforeChecked: Compare? = null
        ): AlertDialog {
            val compareArray = Compare.getMatchArray(dataType)
            val beforeCheckedIndex = beforeChecked?.let { compareArray.indexOf(it) } ?: -1
            var afterCompare: Compare? = null
            return AlertDialog.Builder(context)
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
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
        }
    }
}