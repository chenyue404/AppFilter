package com.chenyue404.appfilter.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chenyue404.appfilter.entry.CompositeCondition

/**
 * Created by cy on 2023/6/26.
 */
class CompositeConditionFragmentVM : ViewModel() {
    private val _condition: MutableLiveData<CompositeCondition> = MutableLiveData()
    val condition: LiveData<CompositeCondition> = _condition

    fun updateCondition(compositeCondition: CompositeCondition) {
        _condition.postValue(compositeCondition)
    }
}