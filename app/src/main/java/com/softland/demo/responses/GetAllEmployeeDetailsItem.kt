package com.softland.demo.responses

data class GetAllEmployeeDetailsItem(

    val address: Address,
    val company: Company,
    val email: String,
    val id: Int,
    val name: String,
    val phone: String,
    val profile_image: String,
    val username: String,
    val website: String
)