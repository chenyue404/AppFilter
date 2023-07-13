package com.chenyue404.appfilter.ui

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import com.chenyue404.androidlib.extends.bind
import com.chenyue404.androidlib.util.json.GsonUtil
import com.chenyue404.androidlib.widget.BaseActivity
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.google.android.material.appbar.MaterialToolbar

/**
 * Created by cy on 2023/7/13.
 */
class CompositeConditionActivity : BaseActivity() {
    companion object {
        const val extra_key_composite_condition = "extra_key_composite_condition"
        const val extra_key_from_position = "extra_key_from_position"
    }

    private val mtb: MaterialToolbar by bind(R.id.mtb)
    private val fcv: FragmentContainerView by bind(R.id.fcv)

    private val filterFragment by lazy { CompositeConditionFragment() }
    private var condition: CompositeCondition? = null
    private var fromPosition = 0

    override fun getContentViewResId() = R.layout.activity_filter
    override fun initView() {
        initToolbar()
        supportFragmentManager.beginTransaction()
            .replace(fcv.id, filterFragment)
            .runOnCommit {
                condition?.let { filterFragment.updateCondition(it) }
            }
            .commit()
    }

    override fun initBeforeSetContent() {
        super.initBeforeSetContent()
        intent.getStringExtra(extra_key_composite_condition)?.let {
            condition = GsonUtil.fromJson(it, CompositeCondition::class.java)
        }
        fromPosition = intent.getIntExtra(extra_key_from_position, 0)
    }

    private fun initToolbar() {
        setSupportActionBar(mtb)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Composite Condition"
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
                                extra_key_composite_condition,
                                GsonUtil.toJson(filterFragment.mCondition)
                            ).putExtra(
                                extra_key_from_position,
                                fromPosition
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

}