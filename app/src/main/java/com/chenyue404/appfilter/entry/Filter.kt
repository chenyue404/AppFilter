package com.chenyue404.appfilter.entry

import com.chenyue404.appfilter.Combination
import com.chenyue404.appfilter.Compare
import com.chenyue404.appfilter.DataName

/**
 * Created by cy on 2023/6/16.
 */
/**
 * 过滤器
 * @param combination 组合单元
 * @param condition 比较单元
 * @param orderBy 用来排序的字段名
 * @param reverse 是否反序
 */
data class Filter(
    val combination: Combination? = null,
    val condition: Condition? = null,
    val orderBy: DataName? = null,
    val reverse: Boolean = false,
)

/**
 * 比较单元
 * @param name 用来比较的字段名
 * @param compare 运算符
 * @param data 值
 */
data class Condition(
    val name: DataName,
    val compare: Compare,
    val data: Any,
)