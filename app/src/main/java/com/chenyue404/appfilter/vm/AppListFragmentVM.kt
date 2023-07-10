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
import com.chenyue404.appfilter.entry.DataType
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

    suspend fun filterList(list: List<PackageInfo>) = withContext(Dispatchers.IO) {
        val packageManager = ContextProvider.mContext.packageManager
        val selector: (PackageInfo) -> Long? = {
            it.lastUpdateTime
        }
        val appItemList = list
            .sortedBy(selector)
            .mapIndexedNotNull { index, packageInfo ->
                _progress.postValue(index + 1)
                if (_filter.value?.condition?.evaluate(packageInfo) != false) {
                    AppListItem.fromPackageInfo(packageManager, packageInfo)
                } else {
                    null
                }
            }.apply {
                if (_filter.value?.reverse == true) {
                    reversed()
                }
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

    private suspend fun applyFilterOnAppItemList() = withContext(Dispatchers.IO) {
        val packageManager = ContextProvider.mContext.packageManager
        val list = _allAppItemList.value ?: listOf()
        val appItemList = list
            .mapIndexedNotNull { index, appItem ->
                val packageInfo = packageManager.getPackageInfo(appItem.packageName, 0)
                if (_filter.value?.condition?.evaluate(packageInfo) != false) {
                    AppListItem.fromPackageInfo(packageManager, packageInfo)
                } else {
                    null
                }
            }
        val appListItems = when (_filter.value?.orderBy?.type) {
            DataType.Boolean -> appItemList.sortedBy {
                val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
                when (_filter.value?.orderBy) {
                    DataName.IsSystem -> packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    DataName.IsDebug -> packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                    DataName.IsTest -> packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY != 0
                    else -> packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY != 0
                }
            }

            DataType.String -> appItemList.sortedBy {
                when (_filter.value?.orderBy) {
                    DataName.PackageName -> it.packageName
                    DataName.VersionName -> it.versionName
                    else -> it.versionName
                }
            }

            DataType.Int -> appItemList.sortedBy {
                val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
                when (_filter.value?.orderBy) {
                    DataName.CompileSdkVersion -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        packageInfo.applicationInfo.compileSdkVersion
                    } else {
                        ReflectionUtil.getFiled("compileSdkVersion", packageInfo).toString().toInt()
                    }

                    DataName.TargetSdkVersion -> packageInfo.applicationInfo.targetSdkVersion
                    DataName.MinSdkVersion -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageInfo.applicationInfo.minSdkVersion
                    } else {
                        0
                    }

                    else -> packageInfo.applicationInfo.targetSdkVersion
                }
            }

            DataType.Long -> appItemList.sortedBy {
                val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
                when (_filter.value?.orderBy) {
                    DataName.FirstInstallTime -> packageInfo.firstInstallTime
                    DataName.LastUpdateTime -> packageInfo.lastUpdateTime
                    DataName.VersionCode -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        packageInfo.versionCode.toLong()
                    }

                    else -> packageInfo.lastUpdateTime
                }
            }

            else -> appItemList.sortedBy {
                val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
                packageInfo.lastUpdateTime
            }
        }.apply {
            if (_filter.value?.reverse == true) {
                asReversed()
            }
        }
        _appItemList.postValue(appListItems)
    }
}