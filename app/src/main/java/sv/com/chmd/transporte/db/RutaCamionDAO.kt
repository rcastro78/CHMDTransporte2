package sv.com.chmd.transporte.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(
    tableName = RutaCamionDAO.TABLE_NAME,
    indices = [Index(value = ["idRuta"], unique = true)]
)
class RutaCamionDAO(@PrimaryKey(autoGenerate = true) val uid: Long,
                    @ColumnInfo(name = "idRuta")  val idRuta: String,
                    @ColumnInfo(name = "nombre")  val nombre: String,
                    @ColumnInfo(name = "camion")  val camion: String,
                    @ColumnInfo(name = "turno")  val turno: String,
                    @ColumnInfo(name = "tipoRuta")  val tipoRuta: String,
                    @ColumnInfo(name = "estatus")  val estatus: String,
                    @ColumnInfo(name = "offline")  val offline: Int) {
    companion object {
        const val TABLE_NAME = "RutaCamionTransporte"
    }

    @Dao
    interface IRutaCamionDAO{
       @Query("SELECT * FROM ${TABLE_NAME} WHERE camion = :c")
       fun getRutaCamion(c: String): List<RutaCamionDAO>
       @Query("DELETE FROM ${TABLE_NAME}")
       fun deleteAll()
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(vararg users: RutaCamionDAO)



    }

}