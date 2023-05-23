package com.example.sps_21.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sps_21.SpectrumData

@Database(entities = [SpectrumData::class], version = 1)
abstract class FilesDatabase:RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        private var instance: FilesDatabase? = null

        @Synchronized
        fun getInstance(applicationContext: Context): FilesDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(applicationContext, FilesDatabase::class.java, "files_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()
            }
            return instance!!
        }

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }
}