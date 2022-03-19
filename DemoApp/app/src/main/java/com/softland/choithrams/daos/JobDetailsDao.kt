package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Carted_item_details
import com.softland.choithrams.entitys.Job_details
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.flow.Flow


@Dao
interface JobDetailsDao {


    @Query("SELECT * FROM Job_details where JobStatus in (2,3) ")
     fun getAll(): Flow<List<Job_details>>

    @Query("SELECT * FROM Job_details")
    fun getAllSavedJobs(): Flow<List<Job_details>>


    @Query("SELECT * FROM Job_details where JobStatus=3")
    fun getPendingApprovalJobs(): Flow<List<Job_details>>

    @Query("SELECT * FROM Job_details where JobStatus in (0,1,4)")
    fun getApprovalStatusJobs(): Flow<List<Job_details>>


    @Query("DELETE  FROM Job_details")
    fun clear(): Int


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend  fun save(bob_details: Job_details)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveAll( job_details: List<Job_details>)

    @Delete
    fun delete(bob_details: Job_details)

    @Query("select * from Job_details where JobStatus in (2,3) and (JobNumber like '%' || :searchText || '%'  or Picker like '%' || :searchText || '%'  or SectionName like '%' || :searchText || '%' )  ")
    fun getAllBySearch(searchText:String): Flow<List<Job_details>>

    @Query("select * from Job_details where  JobNumber like '%' || :searchText || '%'  or SectionCode like '%' || :searchText || '%' or SectionName like '%' || :searchText || '%'  ")
    fun getAllSavedJobBySearch(searchText:String): Flow<List<Job_details>>


    @Query("select * from Job_details where JobStatus=3 and  (JobNumber like '%' || :searchText || '%'  or Picker like '%' || :searchText || '%'  or SectionName like '%' || :searchText || '%' ) ")
    fun getPendingApprovalBySearch(searchText:String): Flow<List<Job_details>>


    @Query("select * from Job_details where JobStatus in  (0,1,4)  and   (JobNumber like '%' || :searchText || '%'  or Picker like '%' || :searchText || '%'  or SectionName like '%' || :searchText || '%' ) ")
    fun getApprovalStatusBySearch(searchText:String): Flow<List<Job_details>>

    @Query("UPDATE Job_details SET JobStatus= :sts where tbl_id= :id ")
    fun updateStatus(sts : Int ,id : String): Int

}