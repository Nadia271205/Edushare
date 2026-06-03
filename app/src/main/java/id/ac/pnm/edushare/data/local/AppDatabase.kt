package id.ac.pnm.edushare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import id.ac.pnm.edushare.data.tugasModel


@Database(entities = [tugasModel::class], version = 1)

abstract class AppDatabase: RoomDatabase() {
    abstract fun tugasDao(): TugasDao
}