package com.chenyue404.appfilter.ui

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.androidlib.util.json.GsonUtil
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
    var mCondition = CompositeCondition()
        private set
    private val compositeConditionActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val strExtra =
                it.data?.getStringExtra(CompositeConditionActivity.extra_key_composite_condition)
                    ?: return@registerForActivityResult
            val compositeCondition = GsonUtil.fromJson(strExtra, CompositeCondition::class.java)
            val position =
                it.data?.getIntExtra(CompositeConditionActivity.extra_key_from_position, 0) ?: 0
            listAdapter.updateItem(compositeCondition, position)
            mCondition.list[position] = compositeCondition
        }

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
        listAdapter.compositeConditionActivityLauncher = {
            compositeConditionActivityLauncher.launch(it)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}