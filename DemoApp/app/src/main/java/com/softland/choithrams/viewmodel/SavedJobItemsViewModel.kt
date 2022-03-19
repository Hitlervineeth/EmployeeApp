package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.softland.choithrams.daos.JobDetailsDao
import com.softland.choithrams.daos.SavedJobItemDetailsDao

import com.softland.choithrams.daos.SectionDetailsDao
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedJobItemsViewModel @ViewModelInject constructor(private val repository: SavedJobItemDetailsDao) : ViewModel() {

    val readData = repository.getAll().asLiveData()


    fun searchJob(searchQuery: String): LiveData<List<Saved_job_item_details>> {
        return repository.getAllBySearch(searchQuery).asLiveData()
    }

    fun updateQty(qty :String,id :String):Int{
        return repository.updateQTY(qty,id)
    }


}