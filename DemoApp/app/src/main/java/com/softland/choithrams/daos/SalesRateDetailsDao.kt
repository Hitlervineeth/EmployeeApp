package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Sales_rate_details
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.flow.Flow


@Dao
interface SalesRateDetailsDao {

    @Query("SELECT * FROM Sales_rate_details")
     fun getAll(): Flow<List<Sales_rate_details>>



    @Query("DELETE  FROM Sales_rate_details")
    fun clear(): Int


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend  fun save(sales_rate_details: Sales_rate_details)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend  fun saveAll(sales_rate_details: List<Sales_rate_details>)



    @Query("select * from Sales_rate_details where StockID = :searchText")
    fun getAllByStockID(searchText:String): Flow<List<Sales_rate_details>>


}