package com.chenyue404.appfilter.entry

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import com.chenyue404.appfilter.Combination
import com.chenyue404.appfilter.Compare
import com.chenyue404.appfilter.DataName
import com.chenyue404.appfilter.DataType
import com.chenyue404.appfilter.cal
import com.chenyue404.appfilter.util.ReflectionUtil

/**
 * Created by cy on 2023/6/16.
 */
/**
 * 过滤器
 * @param orderBy 用来排序的字段名
 * @param reverse 是否反序
 */
data class Filter(
    var condition: Condition? = null,
    val orderBy: DataName? = null,
    val reverse: Boolean = false,
)

interface Condition {
    fun evaluate(packageInfo: PackageInfo): Boolean
}

data class SimpleCondition(
    val name: DataName,
    val compare: Compare,
    val data: Any,
) : Condition {
    override fun evaluate(packageInfo: PackageInfo): Boolean {
        val value = when (name) {
            DataName.CompileSdkVersion ->
                cal(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        packageInfo.applicationInfo.compileSdkVersion
                    } else {
                        packageInfo.getField("compileSdkVersion")
                    }
                )

            DataName.TargetSdkVersion ->
                cal(packageInfo.applicationInfo.targetSdkVersion)

            DataName.MinSdkVersion ->
                cal(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageInfo.applicationInfo.minSdkVersion
                    } else {
                        0
                    }
                )

            DataName.FirstInstallTime ->
                cal(packageInfo.firstInstallTime)

            DataName.LastUpdateTime ->
                cal(packageInfo.lastUpdateTime)

            DataName.PackageName ->
                cal(packageInfo.packageName)

            DataName.VersionName ->
                cal(packageInfo.versionName)

            DataName.VersionCode ->
                cal(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        packageInfo.versionCode
                    }
                )

            DataName.IsSystem ->
                cal(packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)

            DataName.IsDebug ->
                cal(packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)

            DataName.IsTest ->
                cal(packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY != 0)
        }
        return value
    }

    private fun cal(a: Any) = compare.cal(name.type, a, data)
    private fun PackageInfo.getField(filedName: String) =
        ReflectionUtil.getFiled(filedName, this) ?: when (name.type) {
            DataType.Int -> 0
            DataType.Long -> 0L
            DataType.String -> ""
            DataType.Boolean -> false
        }
}

data class CompositeCondition(
    val list: MutableList<Condition>,
    val combination: Combination,
) : Condition {
    override fun evaluate(packageInfo: PackageInfo): Boolean {
        var result = true
        list.forEachIndexed { index, condition ->
            result = if (index == 0) {
                combination.cal(condition.evaluate(packageInfo), null)
            } else {
                combination.cal(result, condition.evaluate(packageInfo))
            }
        }
        return result
    }
}