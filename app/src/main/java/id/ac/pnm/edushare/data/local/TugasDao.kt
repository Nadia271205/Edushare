package id.ac.pnm.edushare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.ac.pnm.edushare.data.TugasModel

@Dao
interface TugasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tugas: TugasModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tugas: List<TugasModel>)

    @Query("SELECT * FROM tugas ORDER BY timestamp DESC")
    suspend fun getAll(): List<TugasModel>

    @Query("DELETE FROM tugas")
    suspend fun clear()
}