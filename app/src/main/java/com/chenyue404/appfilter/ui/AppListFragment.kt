package com.chenyue404.appfilter.ui

import android.content.Intent
import android.graphics.Rect
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.androidlib.extends.dp2Px
import com.chenyue404.androidlib.extends.launch
import com.chenyue404.androidlib.extends.log
import com.chenyue404.androidlib.extends.setOnItemClick
import com.chenyue404.androidlib.util.json.GsonUtil
import com.chenyue404.androidlib.widget.BaseFragment
import com.chenyue404.appfilter.R
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.vm.AppListFragmentVM
import com.chenyue404.appfilter.vm.MainVM
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * Created by cy on 2023/6/16.
 */
class AppListFragment : BaseFragment() {

    private val rvList: RecyclerView by lazy { requireView().findViewById(R.id.rvList) }
    private val progressIndicator: CircularProgressIndicator by lazy { requireView().findViewById(R.id.progressIndicator) }
    private val tvIndicator: TextView by lazy { requireView().findViewById(R.id.tvIndicator) }

    private var miFilter: MenuItem? = null
    private var miSearch: MenuItem? = null
    private var searchView: SearchView? = null

    private val mainVM: MainVM by activityViewModels()
    private val vm: AppListFragmentVM by viewModels()

    private val listAdapter by lazy { AppListAdapter() }
    private val openFilter =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val strExtra = it.data?.getStringExtra(FilterActivity.extra_key_filter)
                ?: return@registerForActivityResult
            val compositeCondition = GsonUtil.fromJson(strExtra, CompositeCondition::class.java)
            vm.updateFilterCondition(compositeCondition)
        }

    override fun getContentViewResId() = R.layout.fragment_app_list
    override fun initView(root: View) {
        initMenu()
        rvList.apply {
            layoutManager = LinearLayoutManager(mContext).apply {
                setHasFixedSize(true)
            }
            adapter = listAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
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

        vm.appItemList.observe(this) {
            listAdapter.update(it)
        }
        vm.progress.observe(this) {
            val allSize = mainVM.infoList.value?.size ?: 0
            val progress = if (allSize > 0) {
                it.toFloat() / allSize
            } else {
                0f
            }
            log("progress=$progress")
            tvIndicator.apply {
                isVisible = it > 0 && allSize > 0 && progress != 1f
                text = "$it/$allSize"
            }
            progressIndicator.apply {
//                isIndeterminate = it == 0
//                this.progress = (progress * 100).roundToInt()
                isVisible = progress != 1f
            }
        }
        mainVM.infoList.observe(this) {
            lifecycle.launch {
                vm.filterList(it)
            }
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar, menu)
                log("onCreateMenu")
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
                searchView = miSearch?.actionView as SearchView?
                searchView?.setOnCloseListener {
                    log("searchview close")
                    false
                }
                searchView?.setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        log("onQueryTextSubmit")
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        log("onQueryTextChange")
                        vm.updateKeyWord(newText ?: "")
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val str = when (menuItem.itemId) {
                    R.id.miFilter -> {
                        openFilter.launch(
                            Intent(mContext, FilterActivity::class.java)
                                .putExtra(
                                    FilterActivity.extra_key_filter,
                                    vm.filter.value?.let { GsonUtil.toJson(it) })
                        )
                        "miFilter"
                    }

                    R.id.miSearch -> "miSearch"
                    else -> "else"
                }
                log(str)
                return str != "else"
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun closeSearchView() {
        miSearch?.let {
            if (it.isActionViewExpanded) {
                it.collapseActionView()
            }
        }
    }
}