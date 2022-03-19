package com.softland.demo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.softland.demo.daos.EmployeeDetailsDao
import com.softland.demo.entitys.Employee_details

@Database(
    entities = [Employee_details::class],
    version = 1
)
abstract class DemoAppDB : RoomDatabase() {

    abstract fun employeeDetailsDao(): EmployeeDetailsDao

    companion object {
        @Volatile
        private var instance: DemoAppDB? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            DemoAppDB::class.java,
            "DemoApp.db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}
