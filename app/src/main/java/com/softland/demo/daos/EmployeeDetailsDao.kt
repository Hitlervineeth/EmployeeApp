package com.softland.demo.daos

import android.provider.ContactsContract
import androidx.room.*
import com.softland.demo.entitys.Employee_details
import com.softland.demo.responses.GetAllEmployeeDetails
import kotlinx.coroutines.flow.Flow


@Dao
interface EmployeeDetailsDao {

    @Query("SELECT * FROM Employee_details")
     fun getAll(): Flow<List<Employee_details>>

    @Query("SELECT * FROM Employee_details where email like '%' || :key || '%' or name like '%' || :key || '%'")
    fun getAllByNameOrEmail(key:String): Flow<List<Employee_details>>

    @Query("SELECT * FROM Employee_details where id=:key")
    fun getAllByEmpID(key:String): Flow<List<Employee_details>>


    @Query("DELETE  FROM Employee_details")
    fun clear(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(employee: Employee_details)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveAll(employees: GetAllEmployeeDetails?)



}