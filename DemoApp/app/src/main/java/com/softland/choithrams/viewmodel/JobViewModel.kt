package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.softland.choithrams.daos.JobDetailsDao

import com.softland.choithrams.daos.SectionDetailsDao
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JobViewModel @ViewModelInject constructor(private val repository: JobDetailsDao) : ViewModel() {

    val readData = repository.getAll().asLiveData()

    val  getallData = repository.getAllSavedJobs().asLiveData()

    val PendingApprovalData = repository.getPendingApprovalJobs().asLiveData()

    val ApprovalStatusData = repository.getApprovalStatusJobs().asLiveData()



    fun searchJob(searchQuery: String): LiveData<List<Job_details>> {
        return repository.getAllBySearch(searchQuery).asLiveData()
    }

    fun getAllSavedJobBySearch(searchQuery: String): LiveData<List<Job_details>> {
        return repository.getAllSavedJobBySearch(searchQuery).asLiveData()
    }


    fun searchPendingApprovalJob(searchQuery: String): LiveData<List<Job_details>> {
        return repository.getPendingApprovalBySearch(searchQuery).asLiveData()
    }

    fun searchApprovalStatusJob(searchQuery: String): LiveData<List<Job_details>> {
        return repository.getApprovalStatusBySearch(searchQuery).asLiveData()
    }

    fun updateStatus(sts:Int,id :String):Int{
        return repository.updateStatus(sts,id)
    }


}