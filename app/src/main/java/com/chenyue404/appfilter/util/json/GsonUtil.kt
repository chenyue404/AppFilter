package com.chenyue404.appfilter.util.json

import com.chenyue404.androidlib.util.json.BooleanTypeAdapter
import com.chenyue404.androidlib.util.json.DoubleTypeAdapter
import com.chenyue404.androidlib.util.json.FloatTypeAdapter
import com.chenyue404.androidlib.util.json.IntegerDefaultAdapter
import com.chenyue404.androidlib.util.json.ListTypeAdapter
import com.chenyue404.androidlib.util.json.LongDefaultAdapter
import com.chenyue404.androidlib.util.json.StringNullAdapter
import com.chenyue404.appfilter.entry.Condition
import com.google.gson.GsonBuilder

val gson by lazy {
    GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Int::class.java, IntegerDefaultAdapter())
        .registerTypeAdapter(String::class.java, StringNullAdapter())
        .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
        .registerTypeAdapter(Double::class.java, DoubleTypeAdapter())
        .registerTypeAdapter(Float::class.java, FloatTypeAdapter())
        .registerTypeAdapter(List::class.java, ListTypeAdapter())
        .registerTypeAdapter(Long::class.java, LongDefaultAdapter())
        .registerTypeAdapter(Condition::class.java, ConditionAdapter())
        .create()
}