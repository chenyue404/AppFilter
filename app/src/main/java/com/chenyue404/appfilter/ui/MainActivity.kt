package com.chenyue404.appfilter.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.FragmentContainerView
import com.chenyue404.androidlib.extends.bind
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.extends.string
import com.chenyue404.androidlib.widget.BaseActivity
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.vm.MainVM
import com.google.android.material.appbar.MaterialToolbar

/**
 * Created by cy on 2023/6/13.
 */
class MainActivity : BaseActivity() {

    private val dlRoot: DrawerLayout by bind(R.id.dlRoot)
    private val mtb: MaterialToolbar by bind(R.id.mtb)
    private val fcv: FragmentContainerView by bind(R.id.fcv)

    private val mainVM: MainVM by viewModels()

    private val allAppsFragment by lazy {
        AppListFragment().apply {
            changeTitleFun = {
                supportActionBar?.title = string(R.string.app_name) + it
            }
        }
    }

    override fun getContentViewResId() = R.layout.activity_main
    override fun initView() {
        initToolBar()
        lifecycle.launch {
            mainVM.queryAllApps()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv, allAppsFragment)
            .commit()
    }

    private fun initToolBar() {
        setSupportActionBar(mtb)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        dlRoot.addDrawerListener(
            ActionBarDrawerToggle(
                this,
                dlRoot,
                0,
                0
            ).apply {
                syncState()
            }
        )
        dlRoot.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                fcv.getFragment<AppListFragment>().closeSearchView()
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == android.R.id.home) {
                    log("Activity home")
                    if (!dlRoot.isDrawerOpen(GravityCompat.START)) {
                        dlRoot.open()
                    }
                    true
                } else
                    false
            }
        })
    }

    override fun onBackPressed() {
        if (dlRoot.isOpen) {
            dlRoot.close()
        } else {
            super.onBackPressed()
        }
    }
}