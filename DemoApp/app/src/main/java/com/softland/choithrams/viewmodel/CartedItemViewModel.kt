package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.softland.choithrams.daos.CartedItemDetailsDao
import com.softland.choithrams.entitys.Carted_item_details
import com.softland.choithrams.entitys.Saved_job_item_details

class CartedItemViewModel @ViewModelInject constructor(private val repository: CartedItemDetailsDao) :
    ViewModel() {

    fun clear() {
        repository.clear()
    }

     fun save(carted_item_details: Carted_item_details): LiveData<List<Carted_item_details>> {
        repository.save(carted_item_details)
        return repository.getAll().asLiveData()

    }

    fun getAll(): LiveData<List<Carted_item_details>> {
        return repository.getAll().asLiveData()
    }

    fun getAllScannedItems(): LiveData<List<Saved_job_item_details>> {
        return repository.getAllScannedItems().asLiveData()
    }

    fun updateQuantityByStockIDAndExpryDate(stockid:Int,exprydate:String,quantity : Double,GrossAmount:Double,NetAmount:Double):  LiveData<List<Carted_item_details>>{
        repository.updateQuantityByStockIDAndExpryDate(stockid,exprydate,quantity,GrossAmount,NetAmount)
        return repository.getAll().asLiveData()
    }


}