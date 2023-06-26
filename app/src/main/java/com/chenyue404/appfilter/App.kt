package com.chenyue404.appfilter

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors
import me.weishu.reflection.Reflection

/**
 * Created by cy on 2023/6/13.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Reflection.unseal(base)
        }
    }
}