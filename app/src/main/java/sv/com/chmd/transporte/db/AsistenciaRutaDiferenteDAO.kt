package sv.com.chmd.transporte.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/*
*  val camion: String,
    val foto: String,
    val id_alumno: String,
    val id_ruta_base_t: String,
    val id_ruta_h_s: String,
    val nombre: String,
    val nombre_ruta: String
* */
@Entity(tableName = AsistenciaRutaDiferenteDAO.TABLE_NAME)
    class AsistenciaRutaDiferenteDAO(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "id_alumno")  val  id_alumno:String,
    @ColumnInfo(name = "id_ruta_base_t")  val  id_ruta_base_t:String,
    @ColumnInfo(name = "id_ruta_h_s")  val  id_ruta_h_s:String,
    @ColumnInfo(name = "camion")  val  camion:String,
    @ColumnInfo(name = "nombre_ruta")  val  nombre_ruta:String,
    @ColumnInfo(name = "nombre")  val  nombre:String,
) {
    companion object {
        const val TABLE_NAME = "RutaDiferente"
    }

    @Dao
    interface iAsistenciaRutaDiferenteDAO {
        @Query("DELETE FROM ${TABLE_NAME}")
        fun clearData()
        @Query("SELECT * FROM ${TABLE_NAME} WHERE id_ruta_base_t = :idRuta")
        fun getAll(idRuta: String): List<AsistenciaRutaDiferenteDAO>
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(asistentes: AsistenciaRutaDiferenteDAO)

    }


}