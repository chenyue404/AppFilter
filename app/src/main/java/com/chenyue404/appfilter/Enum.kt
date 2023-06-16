package com.chenyue404.appfilter

enum class Combination {
    ParenLeft,
    ParenRight,
    And,
    Or,
}

enum class Compare {
    Greater,
    Less,
    Equal,
    Not,
    Contain,
    StartWith,
    EndWith,
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
}