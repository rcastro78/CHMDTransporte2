package sv.com.chmd.transporte.db

import androidx.room.*

@Entity(tableName = RutaDAO.TABLE_NAME)
class RutaDAO(@PrimaryKey(autoGenerate = true) val uid: Long,
              @ColumnInfo(name = "idRuta")  val idRuta: String,
              @ColumnInfo(name = "nombre")  val nombre: String,
              @ColumnInfo(name = "camion")  val camion: String,
              @ColumnInfo(name = "turno")  val turno: String,
              @ColumnInfo(name = "tipoRuta")  val tipoRuta: String,
              @ColumnInfo(name = "estatus")  val estatus: String,
              @ColumnInfo(name = "offline")  val offline: Int){
    companion object {
        const val TABLE_NAME = "RutaTransporte"
    }

    @Dao
    interface IRutaDAO{
        @Query("SELECT * FROM $TABLE_NAME")
        fun getRutas():List<RutaDAO>
        @Query("SELECT * FROM $TABLE_NAME WHERE estatus<2")
        fun getRutasActivas():List<RutaDAO>
        @Insert
        fun guardaRutas(ruta:RutaDAO)
        @Query("DELETE FROM $TABLE_NAME")
        fun delete()
        @Query("UPDATE $TABLE_NAME SET estatus=:estatus,offline=:offline WHERE idRuta=:idRuta")
        fun cambiaEstatusRuta(estatus: String,offline: Int, idRuta: String)

        @Query("UPDATE $TABLE_NAME SET offline=:offline WHERE idRuta=:idRuta")
        fun cambiaOffline(offline: Int, idRuta: String)

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE turno='1' AND estatus<>'2'")
        fun getTotalRutasAbiertasMan():Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE turno='2' AND estatus<>'2'")
        fun getTotalRutasAbiertasTar():Int

        //Rutas que fueron cerradas offline
        @Query("SELECT * FROM $TABLE_NAME WHERE offline=1")
        fun getRutasCerradasOffline():List<RutaDAO>


    }



}