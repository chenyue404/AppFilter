package com.chenyue404.appfilter.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.core.text.isDigitsOnly
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.extends.dp2Px
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.extends.string
import com.chenyue404.androidlib.util.DateUtil
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
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Date

/**
 * Created by cy on 2023/6/26.
 */
class ConditionListAdapter() : RecyclerView.Adapter<ConditionListAdapter.VH>() {

    companion object {
        private const val key_not = "not"
        private const val key_name = "name"
        private const val key_compare = "compare"
        private const val key_data = "data"
        private const val key_combination = "combination"
        private const val key_list = "list"
        private const val type_simple = 0
        private const val type_composite = 1

        fun timeStampToDHMS(time: Long): Array<Number> {
            val days = time / DateUtil.ONE_DAY_TIME
            val hours = (time - days * DateUtil.ONE_DAY_TIME) / DateUtil.ONE_HOUR_TIME
            val minutes =
                (time - days * DateUtil.ONE_DAY_TIME - hours * DateUtil.ONE_HOUR_TIME) / DateUtil.ONE_MIN_TIME
            val seconds =
                (time - days * DateUtil.ONE_DAY_TIME - hours * DateUtil.ONE_HOUR_TIME - minutes * DateUtil.ONE_MIN_TIME) / 1000
            return arrayOf(days, hours, minutes, seconds)
        }
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
        val btData: Button? = view.findViewById(R.id.btData)
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
                val condition = dataList[position] as SimpleCondition
                buildChooseCompareDialog(
                    it.context,
                    condition.name.type, { compare ->
                        onUpdateItem(position, Bundle().apply {
                            putString(key_compare, compare.name)
                        })
                    },
                    condition.compare
                ).show()
            }
            btData?.click {
                val position = bindingAdapterPosition
                val condition = dataList[position] as SimpleCondition
                showEditDataDialog(
                    it.context,
                    condition,
                ) {
                    onUpdateItem(bindingAdapterPosition, Bundle().apply {
                        putString(key_data, it.toString())
                    })
                }

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

        private fun buildChooseCompareDialog(
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
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
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

        private fun showEditDataDialog(
            context: Context,
            condition: SimpleCondition,
            updateListener: ((newData: Any) -> Unit)? = null
        ) {
            val name = condition.name
            val dataType = name.type
            val compare = condition.compare
            val oldData = condition.data
            var rootView: View? = null
            var okListener: (() -> Boolean)? = null
            val dp8 = 8.dp2Px()
            when (dataType) {
                DataType.Int, DataType.Long, DataType.String -> {
                    rootView = EditText(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setText(oldData.toString())
                        setSelection(text.length)
                    }
                    okListener = {
                        KeyboardUtil.hideSoftInput(rootView as EditText)
                        val str = (rootView as EditText).editableText.trim().toString()
                        val contentLegal = str.isNotEmpty() && when (dataType) {
                            DataType.Int -> str.toIntOrNull() != null
                            DataType.Long -> str.toLongOrNull() != null
                            else -> true
                        }
                        if (contentLegal) {
                            updateListener?.invoke(str)
                        }
                        contentLegal
                    }
                }

                DataType.Boolean -> {
                    rootView = ToggleButton(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setPadding(dp8, 0, dp8, 0)
                        }
                        textOn = true.toString()
                        textOff = false.toString()
                        isChecked = oldData == true
                    }
                    okListener = {
                        updateListener?.invoke((rootView as ToggleButton).isChecked)
                        true
                    }
                }

                DataType.Date -> {
                    if (compare == Compare.Greater) {
                        val oldDate = oldData.toString().toLongOrNull()?.let { Date(it) } ?: Date()
                        val calendar = Calendar.getInstance().apply {
                            time = oldDate
                        }
                        val newCalendar = Calendar.getInstance()
                        val timePickListener =
                            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                                log("$hourOfDay, $minute")
                                newCalendar.apply {
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                }
                                updateListener?.invoke(newCalendar.timeInMillis)
                            }
                        val datePickListener =
                            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                                log("$year, $month, $dayOfMonth")
                                newCalendar.apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DATE, dayOfMonth)
                                }
                                TimePickerDialog(
                                    context,
                                    timePickListener,
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        DatePickerDialog(
                            context,
                            datePickListener,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE)
                        ).show()
                    } else {
                        rootView = LayoutInflater.from(context)
                            .inflate(R.layout.layout_relative_time_dailog_content, null)
                        val tooBig = context.string(R.string.tooBig)
                        val inputLayouts = (rootView as ViewGroup).children
                            .filter { it is TextInputLayout }
                        val dhms = timeStampToDHMS((oldData.toString().toLongOrNull() ?: 0L) * 1000)
                        inputLayouts.forEachIndexed { index, inputLayout ->
                            val maxNum = when (index) {
                                1 -> 23
                                2 -> 59
                                3 -> 59
                                else -> Int.MAX_VALUE
                            }
                            (inputLayout as TextInputLayout).editText?.apply {
                                doAfterTextChanged {
                                    if (it?.isDigitsOnly() == false) {
                                        inputLayout.error = context.string(R.string.digitsOnly)
                                        return@doAfterTextChanged
                                    } else {
                                        val str = it?.toString()
                                        val num = str?.toIntOrNull() ?: 0
                                        if (num == 0 && it?.toString()
                                                ?.isEmpty() == false && it.toString() != "0"
                                        ) {
                                            inputLayout.error = tooBig
                                            return@doAfterTextChanged
                                        } else if (num > maxNum) {
                                            inputLayout.error = tooBig
                                            return@doAfterTextChanged
                                        } else {
                                            val numStr = num.toString()
                                            if (str != numStr) {
                                                inputLayout.editText?.apply {
                                                    setText(numStr)
                                                    setSelection(numStr.length)
                                                }
                                            }
                                        }
                                    }
                                    inputLayout.error = null
                                }
                                setText(dhms[index].toString())
                            }
                        }
                        okListener = {
                            KeyboardUtil.hideSoftInput(rootView)
                            var contentLegal = true
                            var totalNum = 0L
                            inputLayouts.forEachIndexed { index, view ->
                                val inputLayout = view as TextInputLayout
                                if (inputLayout.error.isNullOrEmpty()) {
                                    val text = inputLayout.editText?.text.toString()
                                    val num = text.toIntOrNull() ?: 0
                                    val plusNum = when (index) {
                                        0 -> num * DateUtil.ONE_DAY_TIME
                                        1 -> num * DateUtil.ONE_HOUR_TIME
                                        2 -> num * DateUtil.ONE_MIN_TIME
                                        else -> num * 1000L
                                    }
                                    totalNum += plusNum
                                } else {
                                    contentLegal = false
                                }
                            }
                            updateListener?.invoke(totalNum / 1000)
                            contentLegal
                        }
                    }
                }
            }
            rootView ?: return
            AlertDialog.Builder(context)
                .setTitle(name.name)
                .setView(rootView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE).click {
                            val shouldCancel = okListener?.invoke() ?: true
                            if (shouldCancel) cancel()
                        }
                    }
                }
                .show()
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
            holder.btData?.apply {
                text = showDataText(
                    context,
                    condition.data.toString(),
                    condition.name,
                    condition.compare
                )
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
                }
                payload.getString(key_compare)?.let {
                    holder.btCompare?.text = it
                }
                payload.get(key_data)?.let {
                    val dataName = DataName.valueOf(holder.btName?.text.toString())
                    val compare = Compare.valueOf(holder.btCompare?.text.toString())
                    holder.btData?.apply {
                        text = showDataText(context, it.toString(), dataName, compare)
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

    private fun showDataText(
        context: Context,
        dataStr: String,
        dataName: DataName,
        compare: Compare
    ): String {
        val result = if (dataName.type != DataType.Date) {
            dataStr
        } else {
            if (compare == Compare.Greater) {
                DateUtil.timeStampToString(
                    dataStr.toLongOrNull() ?: 0L,
                    "yyyy-MM-dd HH:mm"
                )
            } else {
                val num = (dataStr.toLongOrNull() ?: 0) * 1000L
                val dhms = timeStampToDHMS(num)
                dhms[0].toString() + "d " +
                        dhms[1] + "h " +
                        dhms[2] + "m " +
                        dhms[3] + "s"
            }
        }
        return result
    }
}