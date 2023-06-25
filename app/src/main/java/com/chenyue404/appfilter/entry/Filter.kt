package com.chenyue404.appfilter.entry

import android.content.pm.PackageInfo
import com.chenyue404.appfilter.Compare
import com.chenyue404.appfilter.DataName

/**
 * Created by cy on 2023/6/16.
 */
/**
 * 过滤器
 * @param orderBy 用来排序的字段名
 * @param reverse 是否反序
 */
data class Filter(
//    var entityList: MutableList<FilterEntity> = mutableListOf(),
    val orderBy: DataName? = null,
    val reverse: Boolean = false,
)


/**
 * 比较单元
 * @param name 用来比较的字段名
 * @param compare 运算符
 * @param data 值
 */
//data class Condition(
//    val name: DataName,
//    val compare: Compare,
//    val data: Any,
//)

/**
 * 过滤器单元
 * @param combination 组合单元
 * @param condition 比较单元
 */
//data class FilterEntity(
//    var entity: FilterEntity? = null,
//    var condition: Condition? = null,
//    val combination: Combination? = null,
//)

interface Condition {
    fun evaluate(packageInfo: PackageInfo): Boolean
}

data class SimpleCondition(
    val name: DataName,
    val compare: Compare,
    val data: Any,
) : Condition {
    override fun evaluate(packageInfo: PackageInfo): Boolean {
//        when (name) {
//            DataName.CompileSdkVersion -> compare.cal(name.type,)
//        }
        return false
    }
}