package com.chenyue404.appfilter.vm

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chenyue404.androidlib.ContextProvider
import com.chenyue404.appfilter.entry.AppListItem
import com.chenyue404.appfilter.entry.Condition
import com.chenyue404.appfilter.entry.DataName
import com.chenyue404.appfilter.entry.Filter
import com.chenyue404.appfilter.util.ReflectionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by cy on 2023/6/16.
 */
class AppListFragmentVM : ViewModel() {
    private val _filter: MutableLiveData<Filter> = MutableLiveData()
    val filter: LiveData<Filter> = _filter

    private val _allAppItemList: MediatorLiveData<List<AppListItem>> = MediatorLiveData()
    val allAppItemList: LiveData<List<AppListItem>> = _allAppItemList
    private val _appItemList: MediatorLiveData<List<AppListItem>> =
        MediatorLiveData<List<AppListItem>>().apply {
            addSource(_allAppItemList) {
                viewModelScope.launch {
                    applyFilterOnAppItemList()
                }
            }
            addSource(_filter) {
                viewModelScope.launch {
                    applyFilterOnAppItemList()
                }
            }
        }
    val appItemList: LiveData<List<AppListItem>> = _appItemList

    private val _progress: MutableLiveData<Int> = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    var progressStepStr = ""
        private set
    var progressTotal = 0
        private set

    suspend fun filterList(list: List<PackageInfo>) = withContext(Dispatchers.IO) {
        val packageManager = ContextProvider.mContext.packageManager
        val selector: (PackageInfo) -> Long? = {
            it.lastUpdateTime
        }
//        val appItemList = list
//            .sortedBy(selector)
//            .mapIndexedNotNull { index, packageInfo ->
//                _progress.postValue(index + 1)
//                if (_filter.value?.condition?.evaluate(packageInfo) != false) {
//                    AppListItem.fromPackageInfo(packageManager, packageInfo)
//                } else {
//                    null
//                }
//            }.apply {
//                if (_filter.value?.reverse == true) {
//                    reversed()
//                }
//            }
        progressStepStr = "转换"
        progressTotal = list.size
        val appItemList = list.mapIndexed { index, packageInfo ->
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

    fun updateFilterCondition(condition: Condition) {
        val copy = _filter.value?.copy(condition = condition) ?: Filter(condition)
        _filter.postValue(copy)
    }

    private fun PackageInfo.getCompileSdkVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applicationInfo.compileSdkVersion
        } else {
            ReflectionUtil.getFiled("compileSdkVersion", this).toString()
                .toInt()
        }

    private fun PackageInfo.getMinSdkVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo.minSdkVersion
        } else {
            applicationInfo.targetSdkVersion
        }

    private fun PackageInfo.getVersionCode() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            versionCode.toLong()
        }

    private suspend fun applyFilterOnAppItemList() = withContext(Dispatchers.IO) {
        val packageManager = ContextProvider.mContext.packageManager
        val list = _allAppItemList.value ?: listOf()
        var sortIndex = 1
        var filteredListSize = 0
        progressStepStr = "筛选"
        progressTotal = list.size
        val itemList = list
            .mapIndexedNotNull { index, appItem ->
                _progress.postValue(index + 1)
                val packageInfo = packageManager.getPackageInfo(appItem.packageName, 0)
                if (_filter.value?.condition?.evaluate(packageInfo) != false) {
                    filteredListSize++
                    AppListItem.fromPackageInfo(packageManager, packageInfo)
                } else {
                    null
                }
            }.sortedWith { o1, o2 ->
                progressStepStr = "排序"
                progressTotal = filteredListSize
                _progress.postValue(1 + sortIndex++)
                val o1PkgInfo = o1.getPackageInfo(packageManager)
                val o2PkgInfo = o2.getPackageInfo(packageManager)

                val result = when (_filter.value?.orderBy) {
                    DataName.IsSystem ->
                        (o1PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                            .compareTo(o2PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)

                    DataName.IsDebug ->
                        (o1PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
                            .compareTo(o2PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)

                    DataName.IsTest ->
                        (o1PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY != 0)
                            .compareTo(o2PkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY != 0)

                    DataName.PackageName -> o1.packageName.compareTo(o2.packageName)
                    DataName.VersionName -> o1.versionName.compareTo(o2.versionName)

                    DataName.CompileSdkVersion ->
                        o1PkgInfo.getCompileSdkVersion()
                            .compareTo(o2PkgInfo.getCompileSdkVersion())

                    DataName.TargetSdkVersion ->
                        o1PkgInfo.applicationInfo.targetSdkVersion
                            .compareTo(o2PkgInfo.applicationInfo.targetSdkVersion)

                    DataName.MinSdkVersion ->
                        o1PkgInfo.getMinSdkVersion()
                            .compareTo(o2PkgInfo.getMinSdkVersion())

                    DataName.FirstInstallTime ->
                        o1PkgInfo.firstInstallTime
                            .compareTo(o2PkgInfo.firstInstallTime)

                    DataName.LastUpdateTime ->
                        o1PkgInfo.lastUpdateTime
                            .compareTo(o2PkgInfo.lastUpdateTime)

                    DataName.VersionCode ->
                        o1PkgInfo.getVersionCode()
                            .compareTo(o2PkgInfo.getVersionCode())

                    else -> 0
                }
                return@sortedWith if (_filter.value?.reverse == true) {
                    result * -1
                } else {
                    result
                }
            }
        _appItemList.postValue(itemList)
    }
}