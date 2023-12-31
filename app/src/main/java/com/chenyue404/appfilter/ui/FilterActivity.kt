package com.chenyue404.appfilter.ui

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
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
    private val filterFragment by lazy { CompositeConditionFragment() }

    override fun getContentViewResId() = R.layout.activity_filter
    override fun initView() {
        initToolbar()
        mtb.setNavigationOnClickListener { finish() }
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
    }

    private fun initToolbar() {
        setSupportActionBar(mtb)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Filter"
        }
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_composite_condition_fragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val result = when (menuItem.itemId) {
                    android.R.id.home -> {
                        finish()
                        true
                    }

                    R.id.miSave -> {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(
                                extra_key_filter,
                                GsonUtil.toJson(filterFragment.mCondition)
                            )
                        )
                        finish()
                        true
                    }

                    else -> false
                }
                return result
            }
        }, this, Lifecycle.State.RESUMED)
    }

    override fun initBeforeSetContent() {
        super.initBeforeSetContent()
        intent.getStringExtra(extra_key_filter)?.let {
            vm.updateFilter(GsonUtil.fromJson(it, Filter::class.java))
        }
    }
}