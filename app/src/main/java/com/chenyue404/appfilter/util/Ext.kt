package com.chenyue404.appfilter.util

import android.view.View
import androidx.fragment.app.Fragment

fun <V : View> Fragment.bind(id: Int): Lazy<V> = lazy { requireView().findViewById(id) }