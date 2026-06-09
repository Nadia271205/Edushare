package id.ac.pnm.edushare

import android.app.Application
import id.ac.pnm.edushare.data.local.AppDatabase

class EduShareApp : Application() {
    companion object {
        lateinit var db: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(applicationContext)
    }
}