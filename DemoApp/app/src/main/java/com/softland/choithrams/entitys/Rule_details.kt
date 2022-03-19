package com.softland.choithrams.entitys

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rule_details(

    @ColumnInfo(name = "tbl_id")
    @PrimaryKey(autoGenerate = true)
    var tbl_id: Int,

    val Discount: Double,
    val MCID: Int,
    val RemainingDays: Int,
    val RuleID: Int,
    val RuleCode: String,
    val RuleName: String

    )
