package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Login_details

@Dao
interface LoginDetailsDao {

    @Query("SELECT * FROM Login_details")
    suspend fun getAll(): List<Login_details>


    @Query("DELETE  FROM Login_details")
    fun clear(): Int


    @Insert
    fun insertAll(vararg login_details: Login_details)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  fun save(login_details: Login_details)

    @Delete
    fun delete(login_details: Login_details)


}