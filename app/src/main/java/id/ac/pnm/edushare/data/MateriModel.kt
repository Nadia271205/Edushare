package id.ac.pnm.edushare.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tugas")
data class MateriModel(

    @PrimaryKey
    var id: String = "",
    var title: String = "",
    var category: String = "",
    var description: String = "",
    var fileUrl: String = "",
    var uploaderUid: String = "",
    var uploaderName: String = "",
    var timestamp: Long = 0
)
