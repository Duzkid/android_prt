package com.deus.androidpertama

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Query("SELECT * FROM attendances WHERE user_id = :userId ORDER BY check_in_time DESC")
    suspend fun getAttendancesByUserId(userId: Int): List<AttendanceEntity>

    @Query("SELECT * FROM attendances ORDER BY check_in_time DESC")
    suspend fun getAllAttendances(): List<AttendanceEntity>

    @Query("SELECT * FROM attendances WHERE user_id = :userId AND date(check_in_time/1000, 'unixepoch') = date('now')")
    suspend fun getTodayAttendance(userId: Int): AttendanceEntity?
}

