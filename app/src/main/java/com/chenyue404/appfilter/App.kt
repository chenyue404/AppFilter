package com.chenyue404.appfilter

import android.app.Application
import com.google.android.material.color.DynamicColors

/**
 * Created by cy on 2023/6/13.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}