package com.softland.demo.viewmodel


import android.provider.ContactsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.softland.demo.daos.EmployeeDetailsDao
import com.softland.demo.entitys.Employee_details

class EmployeeViewModel @ViewModelInject constructor(private val repository: EmployeeDetailsDao) : ViewModel() {

    val readData = repository.getAll().asLiveData()


    fun getAllByNameOrEmail(key:String): LiveData<List<Employee_details>> {
        return repository.getAllByNameOrEmail(key).asLiveData()
    }

    fun getByEmpID(key:String): LiveData<List<Employee_details>> {
        return repository.getAllByEmpID(key).asLiveData()
    }





}