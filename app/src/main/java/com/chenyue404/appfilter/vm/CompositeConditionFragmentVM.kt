package com.chenyue404.appfilter.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chenyue404.appfilter.entry.CompositeCondition
import com.chenyue404.appfilter.entry.Condition

/**
 * Created by cy on 2023/6/26.
 */
class CompositeConditionFragmentVM : ViewModel() {
    private val _condition: MutableLiveData<CompositeCondition> = MutableLiveData()
    val condition: LiveData<CompositeCondition> = _condition

    fun updateCondition(compositeCondition: CompositeCondition) {
        _condition.postValue(compositeCondition)
    }

    fun addConditionItem(condition: Condition) {
        val compositeCondition = (_condition.value ?: CompositeCondition()).apply {
            list.add(condition)
        }
        _condition.postValue(compositeCondition)
    }

    fun updateConditionItem(index: Int, condition: Condition) {
        val compositeCondition = _condition.value ?: CompositeCondition()
        val list = compositeCondition.list
        if (index < list.size) {
            list[index] = condition
        } else {
            list.add(condition)
        }
        _condition.postValue(compositeCondition)
    }

    fun deleteConditionItem(index: Int) {
        val compositeCondition = _condition.value ?: CompositeCondition()
        val list = compositeCondition.list
        if (index < list.size) {
            list.removeAt(index)
        }
        _condition.postValue(compositeCondition)
    }

    fun toggleCombination() {
        val compositeCondition = (_condition.value ?: CompositeCondition()).apply {
            combination = combination.getReverse()
        }
        _condition.postValue(compositeCondition)
    }

    fun toggleNot() {
        val compositeCondition = (_condition.value ?: CompositeCondition()).apply {
            not = !not
        }
        _condition.postValue(compositeCondition)
    }
}