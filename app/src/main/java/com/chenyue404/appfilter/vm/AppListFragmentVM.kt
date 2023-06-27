package com.chenyue404.appfilter.vm

import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chenyue404.androidlib.ContextProvider
import com.chenyue404.appfilter.entry.AppListItem
import com.chenyue404.appfilter.entry.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by cy on 2023/6/16.
 */
class AppListFragmentVM : ViewModel() {
    private val _filter: MutableLiveData<Filter> = MutableLiveData()
    val filter: LiveData<Filter> = _filter

    private val _allAppItemList: MediatorLiveData<List<AppListItem>> = MediatorLiveData()
    private val _appItemList: MediatorLiveData<List<AppListItem>> =
        MediatorLiveData<List<AppListItem>>().apply {
            addSource(_allAppItemList) {
                postValue(it)
            }
        }
    val appItemList: LiveData<List<AppListItem>> = _appItemList

    private val _progress: MutableLiveData<Int> = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    suspend fun filterList(list: List<PackageInfo>) = withContext(Dispatchers.IO) {
        val packageManager = ContextProvider.mContext.packageManager
        val selector: (PackageInfo) -> Long? = {
            it.lastUpdateTime
        }
        val appItemList = list
            .sortedByDescending(selector)
            .mapIndexed { index, packageInfo ->
                _progress.postValue(index + 1)
                AppListItem.fromPackageInfo(packageManager, packageInfo)
            }
        _allAppItemList.postValue(appItemList)
    }

    fun updateKeyWord(str: String) {
        _appItemList.postValue(_allAppItemList.value?.filter {
            it.label?.contains(str) == true
                    || it.packageName.contains(str)
        } ?: emptyList())
    }
}