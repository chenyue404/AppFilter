package com.chenyue404.appfilter.util.json

import com.chenyue404.appfilter.entry.Combination
import com.chenyue404.appfilter.entry.Compare
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Condition
import com.chenyue404.appfilter.entry.DataName
import com.chenyue404.appfilter.entry.SimpleCondition
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Created by cy on 2023/6/27.
 */
class ConditionAdapter : JsonDeserializer<Condition> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Condition {
        val jo = json.asJsonObject
        val dataName = jo.get("name")?.asString?.let {
            DataName.valueOf(it)
        }
        val condition =
            if (dataName == null) {
                CompositeCondition(
                    jo.getAsJsonArray("list")?.map {
                        context.deserialize<Condition>(it, Condition::class.java)
                    }?.toMutableList() ?: mutableListOf(),
                    jo.get("combination")?.asString?.let {
                        Combination.valueOf(it)
                    } ?: Combination.And
                )
            } else {
                SimpleCondition(
                    dataName,
                    Compare.valueOf(jo.get("compare").asString),
                    jo.get("data").asString,
                )
            }.apply {
                jo.get("not")?.asBoolean?.let {
                    not = it
                }
            }
        return condition
    }
}