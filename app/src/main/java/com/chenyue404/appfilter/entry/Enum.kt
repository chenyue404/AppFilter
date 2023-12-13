package com.chenyue404.appfilter.entry

import com.chenyue404.appfilter.entry.Compare.Within

enum class Combination(val type: Boolean) {
    And(true),
    Or(false);

    fun cal(a: Boolean, b: Boolean) =
        when (this) {
            And -> a && b
            Or -> a || b
        }

    fun getReverse() = if (type) Or else And
}

/**
 * @property Within 在x天x小时x秒以内
 */
enum class Compare {
    Greater,
    Within,
    Equal,
    Contain,
    Regex;

    fun cal(dataType: DataType, a: Any, b: Any): Boolean {
        return when (this) {
            Greater ->
                if (dataType == DataType.Int) {
                    (a.toString().toInt()) > (b.toString().toInt())
                } else {
                    (a.toString().toLong()) > (b.toString().toLong())
                }

            Within -> System.currentTimeMillis() - a.toString().toLong() <
                    b.toString().toLong() * 1000

            Equal -> a.toString() == b.toString()
            Contain -> a.toString().contains(b.toString(), true)
            Regex -> b.toString().toRegex().containsMatchIn(a.toString())
        }
    }

    companion object {
        fun getMatchArray(dataType: DataType) = when (dataType) {
            DataType.Boolean -> arrayOf(Equal)
            DataType.String -> arrayOf(Equal, Contain, Regex)
            DataType.Int, DataType.Long -> arrayOf(Greater, Equal, Contain, Regex)
            DataType.Date -> arrayOf(Greater, Within)
        }
    }
}

enum class DataType {
    Int,
    String,
    Boolean,
    Long,
    Date;
}

enum class DataName(val type: DataType) {
    CompileSdkVersion(DataType.Int),
    TargetSdkVersion(DataType.Int),
    MinSdkVersion(DataType.Int),

    VersionCode(DataType.Long),

    FirstInstallTime(DataType.Date),
    LastUpdateTime(DataType.Date),

    PackageName(DataType.String),
    VersionName(DataType.String),

    IsSystem(DataType.Boolean),
    IsDebug(DataType.Boolean),
    IsTest(DataType.Boolean),
}