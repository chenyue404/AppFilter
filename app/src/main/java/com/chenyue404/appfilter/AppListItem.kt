package com.chenyue404.appfilter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * Created by cy on 2023/6/13.
 */
data class AppListItem(
    var iconDrawable: Drawable?,
    var packageName: String,
    var label: String,
    var versionCode: Long,
    var versionName: String,
) {
    companion object {
        fun fromPackageInfo(
            pkgManager: PackageManager,
            packageInfo: PackageInfo,
        ) = AppListItem(
            iconDrawable = pkgManager.getApplicationIcon(packageInfo.packageName),
            packageName = packageInfo.packageName,
            label = pkgManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            versionName = packageInfo.versionName,
        )
    }
}
