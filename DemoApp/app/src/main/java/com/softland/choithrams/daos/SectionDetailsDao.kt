package com.softland.choithrams.daos

import androidx.room.*
import com.softland.choithrams.entitys.Section_details
import kotlinx.coroutines.flow.Flow


@Dao
interface SectionDetailsDao {

    @Query("SELECT * FROM section_details")
     fun getAll(): Flow<List<Section_details>>


    @Query("DELETE  FROM section_details")
    fun clear(): Int

    @Insert
    fun insertAll(vararg section_details: Section_details)

    @Insert()
    suspend  fun save(section_details: Section_details)

    @Delete
    fun delete(section_details: Section_details)

    @Query("select * from Section_details where SectionCode like '%' || :searchText || '%' or SectionName like '%' || :searchText || '%'")
    fun getAllBySearch(searchText:String): Flow<List<Section_details>>

    @Query("select * from Section_details where SectionCode = :sectionCode and  SectionName = :sectionName")
    fun getAllBySectionCodeAndSectionName(sectionCode:String,sectionName :String):Flow<List<Section_details>>

}