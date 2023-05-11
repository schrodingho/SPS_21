package com.example.sps_21.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sps_21.SpectrumData
import java.io.File

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: SpectrumData)

    @Delete
    fun deleteData(data: SpectrumData)

    @Query("delete from SpectrumData")
    fun deleteAllData()
    @Query("SELECT * FROM SpectrumData")
    fun loadAllData(): List<SpectrumData>

    @Query("SELECT * FROM SpectrumData ORDER BY id DESC LIMIT 1")
    fun loadLastData(): SpectrumData

}