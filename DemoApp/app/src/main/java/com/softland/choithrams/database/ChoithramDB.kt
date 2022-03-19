package com.softland.choithrams.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.softland.choithrams.daos.*
import com.softland.choithrams.entitys.*

@Database(
    entities = [Carted_item_details::class, Saved_job_item_details::class,Login_details::class, Section_details::class, Stock_details::class, Rule_details::class, Job_details::class,Sales_rate_details::class],
    version = 100
)
abstract class ChoithramDB : RoomDatabase() {

    abstract fun loginDetailsDao(): LoginDetailsDao
    abstract fun sectionDetailsDao(): SectionDetailsDao
    abstract fun stockDetailsDao(): StockDetailsDao
    abstract fun ruleDetailsDao(): RuleDetailsDao
    abstract fun jobDetailsDao(): JobDetailsDao
    abstract fun savedJobItemDetailsDao(): SavedJobItemDetailsDao
    abstract fun salesRateDetailsDao(): SalesRateDetailsDao
    abstract fun cartedItemDetailsDao(): CartedItemDetailsDao

    companion object {
        @Volatile
        private var instance: ChoithramDB? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ChoithramDB::class.java,
            "Choithrams.db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}
