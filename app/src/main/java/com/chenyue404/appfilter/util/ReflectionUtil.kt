package com.chenyue404.appfilter.util

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Field

/**
 * Created by cy on 2023/6/26.
 */
object ReflectionUtil {
    @Throws(NoSuchFieldException::class)
    fun getFiled(name: String, obj: Any): Any? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.getInstanceFields(obj.javaClass)
                .find { (it as Field).name == name } as Field?
        } else {
            obj.javaClass.getField(name)
        }?.apply {
            isAccessible = true
        }?.get(obj)
    }
}