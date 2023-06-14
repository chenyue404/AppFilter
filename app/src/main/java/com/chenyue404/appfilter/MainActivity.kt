package com.chenyue404.appfilter

import android.graphics.Rect
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.chenyue404.androidlib.extends.bind
import com.chenyue404.androidlib.extends.dp2Px
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.extends.setOnItemClick
import com.chenyue404.androidlib.widget.BaseActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * Created by cy on 2023/6/13.
 */
class MainActivity : BaseActivity() {

    private val rvList: RecyclerView by bind(R.id.rvList)
    private val dlRoot: DrawerLayout by bind(R.id.dlRoot)
    private val mtb: MaterialToolbar by bind(R.id.mtb)

    private val mainVM: MainVM by viewModels()

    private val listAdapter by lazy { AppListAdapter() }

    private var miFilter: MenuItem? = null
    private var miSearch: MenuItem? = null
    private val searchView by lazy { miSearch?.actionView as SearchView? }

    override fun getContentViewResId() = R.layout.activity_main
    override fun initView() {

        initToolBar()

        lifecycle.launch {
            mainVM.queryAllApps(mContext)
        }

        rvList.apply {
            layoutManager = LinearLayoutManager(mContext).apply {
                setHasFixedSize(true)
            }
            adapter = listAdapter
            addItemDecoration(object : ItemDecoration() {
                val dp8 = 8.dp2Px()
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    outRect.top = if (position == 0) 0 else dp8
                }
            })
            setOnItemClick(lifecycle, { _, position, _ ->
                Toast.makeText(mContext, listAdapter.dataList[position].label, Toast.LENGTH_SHORT)
                    .show()
            })
        }

        mainVM.appItemList.observe(this) {
            listAdapter.update(it)
        }
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
                miSearch?.let {
                    if (it.isActionViewExpanded) {
                        it.collapseActionView()
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        miFilter = menu.findItem(R.id.miFilter)
        miSearch = menu.findItem(R.id.miSearch)

        miSearch?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                log("onMenuItemActionExpand")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                log("onMenuItemActionCollapse")
                return true
            }
        })
        searchView?.setOnCloseListener {
            log("searchview close")
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val str = when (item.itemId) {
            R.id.miFilter -> "miFilter"
            R.id.miSearch -> "miSearch"
            android.R.id.home -> {
                if (!dlRoot.isDrawerOpen(GravityCompat.START)) {
                    dlRoot.open()
                }
                "home"
            }

            else -> "else"
        }
        log(str)
        return if (str == "else")
            super.onOptionsItemSelected(item)
        else
            true
    }

    override fun onBackPressed() {
        if (dlRoot.isOpen) {
            dlRoot.close()
        } else {
            super.onBackPressed()
        }
    }
}