package com.chenyue404.appfilter.ui

import android.view.View
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.widget.BaseFragment
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Condition
import com.chenyue404.appfilter.entry.SimpleCondition
import com.chenyue404.appfilter.util.bind
import kotlinx.coroutines.delay

/**
 * Created by cy on 2023/6/26.
 */
class CompositeConditionFragment : BaseFragment() {
    private val btNot: ToggleButton by bind(R.id.btNot)
    private val btCombination: ToggleButton by bind(R.id.btCombination)
    private val ivAdd: ImageView by bind(R.id.ivAdd)
    private val rvList: RecyclerView by bind(R.id.rvList)

    private val listAdapter: ConditionListAdapter by lazy { ConditionListAdapter() }
    private var mCondition = CompositeCondition()

    override fun getContentViewResId() = R.layout.fragment_composite_condition
    override fun initView(root: View) {
        initListView()
        loadData()
        ivAdd.click {
            addItem(SimpleCondition.default())
        }
        ivAdd.setOnLongClickListener {
            addItem(CompositeCondition())
            true
        }
        btNot.setOnCheckedChangeListener { _, isChecked ->
            mCondition.not = isChecked
        }
        btCombination.click {
            mCondition.combination = mCondition.combination.getReverse()
        }
        val a = CompositeCondition()
        val b = CompositeCondition()
        log("${a == b}, $a, $b")
    }

    private fun loadData() {
        with(mCondition) {
            btNot.isChecked = not
            btCombination.isChecked = combination.type
            listAdapter.updateList(list)
        }
    }

    private fun initListView() {
        rvList.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = listAdapter
        }
        listAdapter.actionListener = object : ConditionListAdapter.ActionListener {
            override fun update(index: Int, condition: Condition) {
                mCondition.list[index] = condition
            }

            override fun delete(index: Int) {
                mCondition.list.removeAt(index)
            }
        }
    }

    fun updateCondition(compositeCondition: CompositeCondition) {
        mCondition = compositeCondition
        loadData()
    }

    private fun addItem(condition: Condition) {
        mCondition.list.add(condition)
        listAdapter.addItem(condition)
        lifecycle.launch {
            delay(300)
            rvList.scrollToPosition(listAdapter.itemCount - 1)
        }
    }
}