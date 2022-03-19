package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.softland.choithrams.daos.RuleDetailsDao

import com.softland.choithrams.daos.SectionDetailsDao
import com.softland.choithrams.daos.StockDetailsDao
import com.softland.choithrams.entitys.Rule_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.entitys.Stock_details
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RuleViewModel @ViewModelInject constructor(private val repository: RuleDetailsDao) : ViewModel() {

    fun clear(){
        repository.clear()
    }


    fun searchByMCID(mcid : String): LiveData<List<Rule_details>> {
        return repository.getRueByMCID(mcid).asLiveData()
    }



}