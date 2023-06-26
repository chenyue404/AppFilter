package com.chenyue404.appfilter

enum class Combination {
    And,
    Or,
    Not,
}

fun Combination.cal(a: Boolean, b: Boolean?) =
    when (this) {
        Combination.And -> a && (b ?: a)
        Combination.Or -> a || (b ?: a)
        Combination.Not -> !a
    }

enum class Compare {
    Greater,
    Less,
    Equal,
    Contain,
    StartWith,
    EndWith,
}

fun Compare.cal(dataType: DataType, a: Any, b: Any): Boolean {
    return when (this) {
        Compare.Greater -> if (dataType == DataType.Int) {
            (a as Int) > (b as Int)
        } else {
            (a as Long) > (b as Long)
        }

        Compare.Less -> if (dataType == DataType.Int) {
            (a as Int) < (b as Int)
        } else {
            (a as Long) < (b as Long)
        }

        Compare.Equal -> a.toString() == b.toString()
        Compare.Contain -> a.toString().contains(b.toString(), true)
        Compare.StartWith -> a.toString().startsWith(b.toString(), true)
        Compare.EndWith -> a.toString().endsWith(b.toString(), true)
    }
}

enum class DataType {
    Int,
    String,
    Boolean,
    Long,
}

enum class DataName(val type: DataType) {
    CompileSdkVersion(DataType.Int),
    TargetSdkVersion(DataType.Int),
    MinSdkVersion(DataType.Int),

    FirstInstallTime(DataType.Long),
    LastUpdateTime(DataType.Long),

    PackageName(DataType.String),
    VersionName(DataType.String),
    VersionCode(DataType.Long),

    IsSystem(DataType.Boolean),
    IsDebug(DataType.Boolean),
    IsTest(DataType.Boolean),
}