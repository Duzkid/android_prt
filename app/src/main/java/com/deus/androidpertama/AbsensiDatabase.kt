package com.deus.androidpertama

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, AttendanceEntity::class], version = 2, exportSchema = false)
abstract class AbsensiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AbsensiDatabase? = null

        fun getInstance(context: Context): AbsensiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbsensiDatabase::class.java,
                    "absensi_db"
                )
                    .addMigrations(DbMigration.MIGRATION_1_2)
                    // This app is simple; allowing main thread queries to avoid threading setup.
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

