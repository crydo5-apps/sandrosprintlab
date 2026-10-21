package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class PrintLaborDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: PrintLaborDatabase? = null

        fun getDatabase(context: Context): PrintLaborDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrintLaborDatabase::class.java,
                    "sandros_print_labor.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
