package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Section_details(


    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int? = 1,

    @ColumnInfo(name = "UserID") var UserID: Int,
    @ColumnInfo(name = "SectionID") var SectionID: Int,
    @ColumnInfo(name = "SectionCode") var SectionCode: String,
    @ColumnInfo(name = "SectionName") var SectionName: String,
    @ColumnInfo(name = "Description") var Description: String,

    )
