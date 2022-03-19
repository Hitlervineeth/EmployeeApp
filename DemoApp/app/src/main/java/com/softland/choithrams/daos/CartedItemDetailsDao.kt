package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.*
import kotlinx.coroutines.flow.Flow


@Dao
interface CartedItemDetailsDao {

    @Query("SELECT * FROM Carted_item_details")
     fun getAll(): Flow<List<Carted_item_details>>

    @Query("select crt.tbl_id,crt.StockID,sd.StockCode,sd.StockName,crt.BarCodeID,' ' as BarCode,sd.UnitID,sd.UnitCode,sd.UnitName,crt.Quantity,crt.ExpiryDate,crt.ActualRate,crt.RuleRate,crt.RoundOff,00 as Rate,00 as GrossAmount,00 as NetAmount,00 as ApprovedStatus from Carted_item_details crt inner join Stock_details sd on sd.StockID=crt.StockID  ")
    fun getAllScannedItems(): Flow<List<Saved_job_item_details>>

    @Query("DELETE  FROM Carted_item_details")
    fun clear(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(carted_item_details: Carted_item_details)

    @Query("select * from Carted_item_details where StockID = :stockid and ExpiryDate= :exprydate")
    fun getAllByStockIDAndExpryDate(stockid:Int,exprydate:String): List<Carted_item_details>


    @Query("update Carted_item_details set Quantity=Quantity + :quantity , GrossAmount =GrossAmount+ :GrossAmount , NetAmount=NetAmount+ :NetAmount  where StockID = :stockid and ExpiryDate= :exprydate")
    fun updateQuantityByStockIDAndExpryDate(stockid:Int,exprydate:String,quantity : Double,GrossAmount:Double,NetAmount:Double): Int

}