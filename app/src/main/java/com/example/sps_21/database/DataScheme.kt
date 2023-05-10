package com.example.sps_21

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SpectrumData(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "pic_name") val picName: Bitmap?,
    @ColumnInfo(name = "freq_data") val freqData: DoubleArray?,
    @ColumnInfo(name = "loc_id") val locID: Int?
)