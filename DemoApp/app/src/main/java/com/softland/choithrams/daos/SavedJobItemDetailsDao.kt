package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Saved_job_item_details
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.flow.Flow


@Dao
interface SavedJobItemDetailsDao {

    @Query("SELECT * FROM Saved_job_item_details")
     fun getAll(): Flow<List<Saved_job_item_details>>

    @Query("SELECT * FROM Saved_job_item_details")
    fun getnewAll():List<Saved_job_item_details>

    @Query("DELETE  FROM Saved_job_item_details")
    fun clear(): Int


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend  fun save(saved_job_item_details: Saved_job_item_details)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll( saved_job_item_details: List<Saved_job_item_details>)

    @Delete
    fun delete(saved_job_item_details: Saved_job_item_details)

    @Query("select * from Saved_job_item_details where StockName like '%' || :searchText || '%' ")
    fun getAllBySearch(searchText:String): Flow<List<Saved_job_item_details>>

    @Query("UPDATE SAVED_JOB_ITEM_DETAILS SET Quantity= :qty where tbl_id= :id ")
    fun updateQTY(qty : String,id : String): Int



}