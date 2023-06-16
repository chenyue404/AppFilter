package com.chenyue404.appfilter.vm

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chenyue404.androidlib.ContextProvider
import com.chenyue404.androidlib.extends.log
import com.chenyue404.appfilter.entry.AppListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by cy on 2023/6/13.
 */
class MainVM : ViewModel() {

    private val _infoList: MediatorLiveData<List<PackageInfo>> = MediatorLiveData()
    val infoList: LiveData<List<PackageInfo>> = _infoList

    private val _appItemList: MediatorLiveData<List<AppListItem>> = MediatorLiveData()
    val appItemList: LiveData<List<AppListItem>> = _appItemList

    private val _sort: MutableLiveData<(PackageInfo) -> Long?> = MutableLiveData()

    private val _progress: MutableLiveData<Int> = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    var startTime = 0L

    suspend fun queryAllApps() = withContext(Dispatchers.IO) {
        startTime = System.currentTimeMillis()
        val packageManager = ContextProvider.mContext.packageManager
        val infoMutableList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            packageManager.getInstalledPackages(0)
        }
        log((System.currentTimeMillis() - startTime).toString(), "time")
        _infoList.postValue(infoMutableList)
        val selector: (PackageInfo) -> Long? = {
            it.lastUpdateTime
        }
        val appItemList = infoMutableList
            .sortedByDescending(selector)
            .mapIndexed { index, packageInfo ->
                _progress.postValue(index + 1)
                AppListItem.fromPackageInfo(packageManager, packageInfo)
            }
        log((System.currentTimeMillis() - startTime).toString(), "time")
        _appItemList.postValue(appItemList)
    }
}