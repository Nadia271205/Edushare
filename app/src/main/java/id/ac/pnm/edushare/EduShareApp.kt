package id.ac.pnm.edushare

import android.app.Application
import androidx.room.Room
import id.ac.pnm.edushare.data.local.AppDatabase

class EduShareApp : Application() {
    companion object {

        lateinit var db: AppDatabase

    }

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "edushare_db"
        ).build()
    }
}