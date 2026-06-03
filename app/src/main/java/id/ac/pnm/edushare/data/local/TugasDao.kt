package id.ac.pnm.edushare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.ac.pnm.edushare.data.tugasModel

@Dao
interface TugasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tugas: tugasModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tugas: List<tugasModel>)

    @Query("SELECT * FROM tugas ORDER BY timestamp DESC")
    suspend fun getAll(): List<tugasModel>

    @Query("DELETE FROM tugas")
    suspend fun clear()
}