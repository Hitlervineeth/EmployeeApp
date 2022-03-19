package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import com.softland.choithrams.daos.SectionDetailsDao
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SectionViewModel @ViewModelInject constructor(private val repository: SectionDetailsDao) : ViewModel() {

    val readData = repository.getAll().asLiveData()

    fun insertData(section_details: Section_details){
        viewModelScope.launch(Dispatchers.IO) {
            repository.save(section_details)
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<Section_details>> {
        return repository.getAllBySearch(searchQuery).asLiveData()
    }

    fun searchBySectionCodeAndSectionName(sectionCode : String, sectionName : String): LiveData<List<Section_details>> {
        return repository.getAllBySectionCodeAndSectionName(sectionCode,sectionName).asLiveData()
    }

}