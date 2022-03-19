package com.softland.demo.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.softland.demo.responses.Address
import com.softland.demo.responses.Company

@Entity
data class Employee_details(

    @PrimaryKey(autoGenerate = true)
    val tbl_id: Int?=1,

    val email: String,
    val id: Int,
    val name: String,
    val phone: String,
    val profile_image: String,
    val username: String,
    val website: String

)
