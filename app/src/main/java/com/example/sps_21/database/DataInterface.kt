package com.example.sps_21.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sps_21.SpectrumData

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile(file: SpectrumData)

    @Delete
    fun deleteFile(file: SpectrumData)

    @Query("SELECT * FROM SpectrumData")
    fun loadAllFiles(): List<SpectrumData>
}