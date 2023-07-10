package com.chenyue404.appfilter.entry

import android.content.Context
import com.chenyue404.appfilter.R

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

enum class Compare {
    Greater,
    Less,
    Equal,
    Contain,
    StartWith,
    EndWith;

    fun cal(dataType: DataType, a: Any, b: Any): Boolean {
        return when (this) {
            Compare.Greater -> if (dataType == DataType.Int) {
                (a.toString().toInt()) > (b.toString().toInt())
            } else {
                (a.toString().toLong()) > (b.toString().toLong())
            }

            Compare.Less -> if (dataType == DataType.Int) {
                (a.toString().toInt()) < (b.toString().toInt())
            } else {
                (a.toString().toLong()) < (b.toString().toLong())
            }

            Compare.Equal -> a.toString() == b.toString()
            Compare.Contain -> a.toString().contains(b.toString(), true)
            Compare.StartWith -> a.toString().startsWith(b.toString(), true)
            Compare.EndWith -> a.toString().endsWith(b.toString(), true)
        }
    }

    companion object {
        fun getMatchArray(dataType: DataType) = when (dataType) {
            DataType.Boolean -> arrayOf(Equal)
            DataType.String -> arrayOf(Equal, Contain, StartWith, EndWith)
            DataType.Int -> values()
            DataType.Long -> values()
        }
    }
}

enum class DataType {
    Int,
    String,
    Boolean,
    Long;

    fun getHintText(context: Context) =
        when (this) {
            Boolean -> context.getString(R.string.true_or_false)
            String -> context.getString(R.string.text)
            else -> context.getString(R.string.number)
        }
}

enum class DataName(val type: DataType) {
    CompileSdkVersion(DataType.Int),
    TargetSdkVersion(DataType.Int),
    MinSdkVersion(DataType.Int),

    FirstInstallTime(DataType.Long),
    LastUpdateTime(DataType.Long),
    VersionCode(DataType.Long),

    PackageName(DataType.String),
    VersionName(DataType.String),

    IsSystem(DataType.Boolean),
    IsDebug(DataType.Boolean),
    IsTest(DataType.Boolean),
}