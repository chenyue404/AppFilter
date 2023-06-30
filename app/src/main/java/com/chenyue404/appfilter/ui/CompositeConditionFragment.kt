package com.chenyue404.appfilter.ui

import android.view.View
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.widget.BaseFragment
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.SimpleCondition
import com.chenyue404.appfilter.util.bind
import com.chenyue404.appfilter.vm.CompositeConditionFragmentVM

/**
 * Created by cy on 2023/6/26.
 */
class CompositeConditionFragment : BaseFragment() {
    private val btNot: ToggleButton by bind(R.id.btNot)
    private val btCombination: ToggleButton by bind(R.id.btCombination)
    private val ivAdd: ImageView by bind(R.id.ivAdd)
    private val rvList: RecyclerView by bind(R.id.rvList)

    private val vm: CompositeConditionFragmentVM by viewModels()
    private val listAdapter: ConditionListAdapter by lazy { ConditionListAdapter() }

    override fun getContentViewResId() = R.layout.fragment_composite_condition
    override fun initView(root: View) {
        initListView()
        vm.condition.observe(this) {
            btNot.isChecked = it.not
            btCombination.isChecked = it.combination.type
            listAdapter.updateList(it.list)
        }
        ivAdd.click {
            vm.addConditionItem(SimpleCondition.default())
        }
        ivAdd.setOnLongClickListener {
            vm.addConditionItem(CompositeCondition())
            true
        }
        btNot.setOnCheckedChangeListener { _, _ ->
            vm.toggleNot()
        }
        btCombination.click {
            vm.toggleCombination()
        }
    }

    private fun initListView() {
        rvList.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = listAdapter
        }
    }

    fun updateCondition(compositeCondition: CompositeCondition) {
        vm.updateCondition(compositeCondition)
    }
}