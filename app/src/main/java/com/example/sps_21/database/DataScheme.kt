package com.example.sps_21

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity
data class SpectrumData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pcm_bytes") val pcmBytes: ByteArray?,
    @ColumnInfo(name = "loc_id") val locID: String?
)