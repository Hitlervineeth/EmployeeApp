package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Sales_rate_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.entitys.Stock_details
import kotlinx.coroutines.flow.Flow


@Dao
interface StockDetailsDao {

    @Query("SELECT * FROM stock_details")
     fun getAll(): Flow<List<Stock_details>>


    @Query("DELETE  FROM stock_details")
    fun clear(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveAll(stock_details: List<Stock_details> )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend  fun save(stock_details: Stock_details)


    @Delete
    fun delete(stock_details: Stock_details)

    @Query("select std.* from stock_details std inner join Sales_rate_details srd on srd.StockID==std.StockID where srd.BarCode= :barcodeNumber")
    fun getAllByBarcode(barcodeNumber:String):Flow<List<Stock_details>>

    @Query("select * from Sales_rate_details where BarCode= :barcodeNumber")
    fun getAllSalesRateDetailsByBarcode(barcodeNumber:String):Flow<List<Sales_rate_details>>

}