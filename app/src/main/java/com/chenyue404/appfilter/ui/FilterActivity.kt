package com.chenyue404.appfilter.ui

import androidx.activity.viewModels
import androidx.fragment.app.FragmentContainerView
import com.chenyue404.androidlib.extends.bind
import com.chenyue404.androidlib.util.json.GsonUtil
import com.chenyue404.androidlib.widget.BaseActivity
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Filter
import com.chenyue404.appfilter.vm.FilterActivityVM
import com.google.android.material.appbar.MaterialToolbar

/**
 * Created by cy on 2023/6/26.
 */
class FilterActivity : BaseActivity() {
    companion object {
        const val extra_key_filter = "extra_key_filter"
    }

    private val mtb: MaterialToolbar by bind(R.id.mtb)
    private val fcv: FragmentContainerView by bind(R.id.fcv)

    private val vm: FilterActivityVM by viewModels()

    override fun getContentViewResId() = R.layout.activity_filter
    override fun initView() {
        mtb.setNavigationOnClickListener { finish() }
        val filterFragment = CompositeConditionFragment()
        supportFragmentManager.beginTransaction()
            .replace(fcv.id, filterFragment)
            .runOnCommit {
            }
            .commit()
        vm.filter.observe(this) {
            filterFragment.updateCondition(
                (it?.condition as CompositeCondition?)
                    ?: CompositeCondition()
            )
        }

//        val filter1 = Filter(
//            CompositeCondition(
//                mutableListOf(
//                    SimpleCondition(
//                        DataName.PackageName,
//                        Compare.Contain,
//                        "123"
//                    )
//                )
//            )
//        )
//        log(filter1.toString())
//        log(GsonUtil.toJson(filter1))
    }

    override fun initBeforeSetContent() {
        super.initBeforeSetContent()
        intent.getStringExtra(extra_key_filter)?.let {
            vm.updateFilter(
                GsonUtil.fromJson(it, Filter::class.java)
            )
        }
    }
}