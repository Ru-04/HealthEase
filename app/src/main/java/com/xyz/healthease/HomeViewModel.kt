package com.xyz.healthease

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xyz.healthease.ApiService

class HomeViewModel : ViewModel() {
    private val _familyList = MutableLiveData<List<ApiService.FamilyMember>>()
    val familyList: LiveData<List<ApiService.FamilyMember>> get() = _familyList

    fun updateFamilyList(newFamily: ApiService.FamilyMember) {
        val updatedList = _familyList.value.orEmpty().toMutableList()
        updatedList.add(newFamily)
        _familyList.value = updatedList
    }
}