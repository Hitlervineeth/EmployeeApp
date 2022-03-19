package com.softland.choithrams.daos

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.softland.choithrams.entitys.Rule_details
import com.softland.choithrams.entitys.Section_details
import com.softland.choithrams.entitys.Stock_details
import kotlinx.coroutines.flow.Flow


@Dao
interface RuleDetailsDao {

    @Query("SELECT * FROM rule_details")
     fun getAll(): Flow<List<Rule_details>>

    @Query("SELECT * FROM rule_details")
    fun getAllNew(): List<Rule_details>


    @Query("DELETE  FROM rule_details")
    fun clear(): Int


    @Insert(onConflict = REPLACE)
    suspend  fun save(rule_details: Rule_details)

    @Delete
    fun delete(rule_details: Rule_details)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(vararg rule_details: Rule_details?)

    @Query("SELECT * FROM rule_details where MCID= :mcid ORDER BY RemainingDays ")
    fun getRueByMCID(mcid:String): Flow<List<Rule_details>>

    @Query("SELECT * FROM rule_details where MCID= :mcid ORDER BY RemainingDays ")
    fun getRueByMCIDNew(mcid:String): List<Rule_details>



}