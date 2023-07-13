package com.chenyue404.appfilter.entry

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * Created by cy on 2023/6/13.
 */
data class AppListItem(
    var packageName: String,
    var versionCode: Long,
    var versionName: String,
    var label: String? = null,
    var iconDrawable: Drawable? = null,
) {
    companion object {
        fun fromPackageInfo(
            pkgManager: PackageManager,
            packageInfo: PackageInfo,
        ) = AppListItem(
            packageName = packageInfo.packageName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            versionName = packageInfo.versionName,
            label = pkgManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
            iconDrawable = pkgManager.getApplicationIcon(packageInfo.packageName),
        )
    }

    fun getPackageInfo(pkgManager: PackageManager): PackageInfo =
        pkgManager.getPackageInfo(packageName, 0)
}
