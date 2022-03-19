package com.softland.choithrams.viewmodel


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import com.softland.choithrams.daos.SectionDetailsDao
import com.softland.choithrams.daos.StockDetailsDao
import com.softland.choithrams.entitys.Sales_rate_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.entitys.Stock_details
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockViewModel @ViewModelInject constructor(private val repository: StockDetailsDao) : ViewModel() {

    fun clear(){
        repository.clear()
    }


    fun searchByBarcode(barcodeNumber : String): LiveData<List<Stock_details>> {
        return repository.getAllByBarcode(barcodeNumber).asLiveData()
    }

    fun searchSalesRateByBarcode(barcodeNumber : String): LiveData<List<Sales_rate_details>> {
        return repository.getAllSalesRateDetailsByBarcode(barcodeNumber).asLiveData()
    }


}