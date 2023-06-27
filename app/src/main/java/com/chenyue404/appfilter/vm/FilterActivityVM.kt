package com.chenyue404.appfilter.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chenyue404.appfilter.entry.Filter

/**
 * Created by cy on 2023/6/27.
 */
class FilterActivityVM : ViewModel() {
    private val _filter: MutableLiveData<Filter?> = MutableLiveData()
    val filter: LiveData<Filter?> = _filter

    fun updateFilter(filter: Filter) {
        _filter.postValue(filter)
    }
}