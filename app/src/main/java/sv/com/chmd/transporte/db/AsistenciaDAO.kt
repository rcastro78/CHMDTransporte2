package sv.com.chmd.transporte.db

import androidx.room.*

@Entity(tableName = AsistenciaDAO.TABLE_NAME)
class AsistenciaDAO(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "idRuta")  val  idRuta:String,
    @ColumnInfo(name = "tarjeta")  val  tarjeta:String,
    @ColumnInfo(name = "idAlumno")  val  idAlumno:String,
    @ColumnInfo(name = "nombreAlumno")  val  nombreAlumno:String,
    @ColumnInfo(name = "domicilio")  val  domicilio:String,
    @ColumnInfo(name = "horaManana")  val  horaManana:String,
    @ColumnInfo(name = "horaRegreso")  val  horaRegreso:String,
    @ColumnInfo(name = "ascenso")  val  ascenso:String,
    @ColumnInfo(name = "descenso")  val  descenso:String,
    @ColumnInfo(name = "domicilio_s")  val  domicilio_s:String,
    @ColumnInfo(name = "grupo")  val  grupo:String,
    @ColumnInfo(name = "grado")  val  grado:String,
    @ColumnInfo(name = "nivel")  val  nivel:String,
    @ColumnInfo(name = "foto")  val  foto:String,
    @ColumnInfo(name = "selected")  val  selected:Boolean,
    @ColumnInfo(name = "selectedNA")  val  selectedNA:Boolean,
    @ColumnInfo(name = "ascenso_t")  val  ascenso_t:String?="0",
    @ColumnInfo(name = "descenso_t")  val  descenso_t:String="0",
    @ColumnInfo(name = "salida")  val  salida:String,
    @ColumnInfo(name = "ordenIn")  val  ordenIn:String="0",
    @ColumnInfo(name = "ordenOut")  val  ordenOut:String="0",
    @ColumnInfo(name = "inasist")  val  inasist:Boolean,
    @ColumnInfo(name = "inasistTarde")  val  inasistTarde:Boolean,
    @ColumnInfo(name = "procesado")  val  procesado:Int,
    @ColumnInfo(name = "asistencia")  val  asistencia:String,
    @ColumnInfo(name = "horaRegistro")  val  horaRegistro:String,
    @ColumnInfo(name = "ordenEspecial")  val  ordenEspecial:String="0",
    @ColumnInfo(name = "porSalir")  val  porSalir:String="0",
    @ColumnInfo(name = "ordenIn1")  val  ordenIn1:String="0",
@ColumnInfo(name = "ordenOut1")  val  ordenOut1:String="0",
) {
    companion object {
        const val TABLE_NAME = "Asistencia"
    }

    @Dao
    interface IAsistenciaDAO {

        @Query("SELECT * FROM $TABLE_NAME WHERE tarjeta=:tarjeta AND idRuta=:idRuta")
        fun getAlumnoByTarjeta(tarjeta: String,idRuta:String):List<AsistenciaDAO>

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta ORDER BY CAST(asistencia AS INTEGER) DESC,CAST(ascenso as INTEGER), CAST(descenso as INTEGER),CAST(ordenIn as INTEGER), CAST(salida as INTEGER)")
        fun getAsistencia(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE procesado=-1")
        fun getTotalNoProcesados():Int

        @Query("SELECT DISTINCT(domicilio) FROM $TABLE_NAME WHERE idRuta=:idRuta AND cast(asistencia as integer)>0")
        fun getDirecciones(idRuta: String):List<String>

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND cast(asistencia as integer)>0 AND asistencia=1 " +
                "ORDER BY CAST(ascenso_t as INTEGER), " +
                "CASE  WHEN CAST(ordenOut AS INTEGER) > 900 THEN 0 ELSE 1 END," +
                "CAST(salida as INTEGER)," +
                "CAST(ordenOut as INTEGER) DESC," +
                "CAST(ascenso_t as INTEGER)," +
                "CAST(descenso_t as INTEGER), " +
                "CAST(idAlumno as INTEGER)")
        fun getAsistenciaTarde(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1'")
        fun getTotalidadAsistenciaEnBus(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1' AND ascenso='1' AND descenso='0'")
        fun getTotalidadAsistenciaEnBusBajada(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1' AND ascenso_t='1' AND descenso_t='0'")
        fun getTotalidadAsistenciaEnBusBajadaTarde(idRuta: String):Int

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND nombreAlumno LIKE '%' || :nombre || '%' ORDER BY CAST(ascenso as INTEGER), CAST(ordenIn as INTEGER)")
        fun buscarAlumnos(idRuta: String,nombre:String):List<AsistenciaDAO>


        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND ascenso<2 and descenso<2 ORDER BY CAST(descenso as INTEGER),CAST(salida as INTEGER), CAST(descenso_t as INTEGER), CAST(ordenIn as INTEGER)")
        fun getAlumnosBajar(idRuta: String):List<AsistenciaDAO>

        //@Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta order by CAST(ordenEspecial as INTEGER) ASC,CAST(ascenso_t as integer) asc, cast(descenso_t as integer) asc,CAST(ordenOut as INTEGER) ASC")
        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta and ascenso<2 and descenso<2 and  ascenso_t<2 and descenso_t<2 and salida<2 and asistencia>0 order by CAST(descenso_t AS UNSIGNED),(CASE WHEN CAST(ordenOut AS UNSIGNED) > 900 THEN 0 ELSE 1 END), CAST(salida AS UNSIGNED),CAST(ordenout AS UNSIGNED) DESC,CAST(idAlumno AS UNSIGNED)")
        fun getAlumnosBajarTarde(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta order by CAST(descenso_t AS UNSIGNED),(CASE WHEN CAST(ordenout AS UNSIGNED) > 900 THEN 0 ELSE 1 END), CAST(salida AS UNSIGNED),CAST(ordenout AS UNSIGNED) DESC,CAST(idAlumno AS UNSIGNED)")
        fun getAlumnosSubirTarde(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND CAST(asistencia as INTEGER)=0")
        fun getTotalNoAsisten(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND CAST(asistencia as INTEGER)=1 AND ascenso=1")
        fun getTotalidadRutaAlumnosMan(idRuta: String):Int


        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND ascenso_t=1 and descenso_t=1")
        fun getTotalBajanTarde(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND ascenso_t=1 and descenso_t=0")
        fun getTotalSubenTarde(idRuta: String):Int

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun guardaAsistencia(asistenciaDAO: AsistenciaDAO)




        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun existeTarjeta(tarjeta: String,idRuta: String):Int

        @Query("UPDATE $TABLE_NAME SET ordenEspecial=:ordenEspecial WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun cambiaOrdenEspecial(ordenEspecial: Int, idRuta: String,idAlumno: String)

        @Query("UPDATE $TABLE_NAME SET porSalir=:porSalir WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun setSalidaEspecial(porSalir: Int, idRuta: String,idAlumno: String)

        //turno mañana
        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno AND ascenso=0 AND descenso=0")
        fun asisteTurnoMan(idRuta: String, idAlumno: String,procesado:Int, horaRegistro:String)


        @Query("UPDATE $TABLE_NAME SET asistencia=:asistencia,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun cambiaAsistencia(idRuta: String, idAlumno: String,asistencia:String,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND tarjeta=:tarjeta AND ascenso=0 AND descenso=0")
        fun asisteTurnoManNFC(idRuta: String, tarjeta: String,procesado:Int,horaRegistro: String)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND ascenso<2 AND descenso<2")
        fun asistenTodosMan(idRuta: String,procesado:Int,horaRegistro: String)

        @Query("UPDATE $TABLE_NAME SET descenso=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND ascenso<2 AND descenso<2")
        fun bajanTodosMan(idRuta: String,procesado:Int,horaRegistro: String)

        @Query("UPDATE $TABLE_NAME SET ascenso=0 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaAsistenciaMan(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaSubidaMan(idRuta: String, idAlumno: String, procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso=0 , descenso=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta")
        fun reiniciaAsistenciaRutaMan(idRuta: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET descenso=0, procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaBajadaMan(idRuta: String,idAlumno: String, procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=2, descenso=2,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun noAsisteTurnoMan(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=1,procesado=:procesado ,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun bajaTurnoMan(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun bajaTurnoManNFC(idRuta: String, tarjeta: String,procesado: Int,horaRegistro:String)

        //turno tarde
        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun asisteTurnoTar(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun asisteTurnoTarNFC(idRuta: String, tarjeta: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND ascenso_t<2 AND descenso_t<2 AND asistencia==1 AND salida=0")
        fun asistenTodosTar(idRuta: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET descenso_t=1,ascenso_t=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND ascenso_t<2 AND descenso_t<2")
        fun bajanTodosTar(idRuta: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=0 , descenso_t=0,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaAsistenciaTar(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0, salida=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaBajadaTar(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=2, descenso_t=2,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun noAsisteTurnoTar(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun bajaTurnoTar(idRuta: String, idAlumno: String,procesado: Int,horaRegistro:String)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND ascenso_t<2 AND descenso_t<2")
        fun subenTodosTurnoTar(idRuta: String,procesado: Int,horaRegistro:String)



        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=1,procesado=:procesado,horaRegistro=:horaRegistro WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun bajaTurnoTarNFC(idRuta: String, tarjeta: String,procesado: Int,horaRegistro:String)

        //elimina asistencia
        @Query("DELETE FROM $TABLE_NAME WHERE idRuta=:idRuta")
        fun eliminaAsistencia(idRuta:String)

        @Query("DELETE FROM $TABLE_NAME")
        fun eliminaAsistenciaCompleta()

        //Procesamiento offline
        @Query("SELECT * FROM $TABLE_NAME WHERE procesado=-1")
        fun getAsistenciaSP():List<AsistenciaDAO>
        @Query("UPDATE $TABLE_NAME SET procesado=1 WHERE idAlumno=:idAlumno and idRuta=:idRuta")
        fun actualizaProcesados(idRuta: String, idAlumno: String)

        //funciones para prueba
        @Query("UPDATE $TABLE_NAME SET ordenOut='998' WHERE ordenOut='15'")
        fun updateTest()
        @Query("UPDATE $TABLE_NAME SET ordenEspecial='1' where ordenOut='998'")
        fun updateTest2()

    }

}