package id.ac.pnm.edushare.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.ac.pnm.edushare.data.MateriModel

@Dao
interface MateriDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tugas: MateriModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tugas: List<MateriModel>)

    @Query("SELECT * FROM tugas ORDER BY timestamp DESC")
    suspend fun getAll(): List<MateriModel>

    @Delete
    suspend fun delete(tugas: MateriModel)

    @Query("DELETE FROM tugas")
    suspend fun clear()
}