package sv.com.chmd.transporte.networking


import retrofit2.Call
import retrofit2.http.*
import sv.com.chmd.transporte.model.*

interface ITransporte {
    @GET("getRutaTransporte2.php")
    fun getRutaTransporte(@Query("aux_id") aux_id: String?): Call<List<Ruta>>

    @GET("validarSesion.php")
    fun iniciarSesion(
        @Query("usuario") email: String?,
        @Query("clave") clave: String?
    ): Call<List<Usuario>>


    @POST("getRutaTransporteCamion3.php")
    @FormUrlEncoded
    fun getRutasCamion(@Field("camion") camion: String?):Call<List<Ruta>>

    @POST("getRutaTransporteCamion2.php")
    @FormUrlEncoded
    fun getRutasCamion(@Field("camion") camion: String?, @Field("clave") clave: String?):Call<List<Ruta>>

    //Alumnos en ruta

    @GET("getAlumnosRutaMat.php")
    fun getAsistenciaMan(@Query("ruta_id") idRuta: String?,@Query("token") token: String?):Call<List<Asistencia>>

    @GET("getAlumnosRutaMatBajar.php")
    fun getAsistenciaManBajar(@Query("ruta_id") idRuta: String?,@Query("token") token: String?):Call<List<Asistencia>>


    @GET("getAlumnosRutaTar.php")
    fun getAsistenciaTar(@Query("ruta_id") idRuta: String?,@Query("token") token: String?):Call<List<Asistencia>>

    @GET("getAlumnosRutaTarBajar.php")
    fun getAsistenciaTarBajar(@Query("ruta_id") idRuta: String?,@Query("token") token: String?):Call<List<Asistencia>>

    //Alumnos de otras rutas
    @GET("getAlumnosRutaDiferente2.php")
    fun getAlumnosRutaDiferente(@Query("oldruta_id") idRuta: String?):Call<List<AlumnoRutaDiferenteItem>>


    //Marcar asistencia
    @GET("asistenciaAlumno.php")
    fun asistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?,
                            @Query("hora") hora:String, @Query("token") token: String):Call<String>

    //Marcar bajada
//Marcar asistencia
    @GET("descensoAlumno.php")
    fun descensoAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?,
                          @Query("token") token: String?):Call<String>

    @GET("bajarTodosMan.php")
    fun bajarTodosMan(@Query("id_ruta") idRuta: String?, @Query("hora") hora:String):Call<String>

    @GET("subirTodosTar.php")
    fun subirTodosTar(@Query("id_ruta") idRuta: String?, @Query("hora") hora:String):Call<String>

    //Marcar asistencia
    @GET("ascensoTodosMan.php")
    fun ascensoTodosAlumnos(@Query("id_ruta") idRuta: String?, @Query("hora") hora:String):Call<String>

    //Enviar comentario
    @GET("registraComentario.php")
    fun enviarComentario(@Query("id_ruta") idRuta: String?,@Query("comentario") comentario: String?):Call<String>
    @GET("getComentario.php")
    fun getComentario(@Query("id_ruta") idRuta: String?):Call<List<Comentario>>

    @GET("asistenciaAlumnoTarde.php")
    fun asistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?, @Query("hora") hora:String):Call<String>

    @GET("descensoAlumnoTarde.php")
    fun descensoAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?,@Query("hora") hora:String):Call<String>

    //Marcar inasistencia
    @GET("noAsistenciaAlumno.php")
    fun inasistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>
    @GET("noAsistenciaAlumnoTarde.php")
    fun inasistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //reinicio de asistencia
    @GET("reiniciaAsistenciaAlumno.php")
    fun reiniciaAsistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    @GET("reiniciaAsistenciaAlumnoTarde.php")
    fun reiniciaAsistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Marcar asistencia
    @GET("descensoTodosTar.php")
    fun descensoTodosAlumnoTar(@Query("id_ruta") idRuta: String?):Call<String>

    @GET("reiniciarBajada.php")
    fun reiniciarBajada(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    @GET("reiniciarBajadaMan.php")
    fun reiniciarBajadaMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Cerrar ruta

    @GET("cerrarRuta3_TEST.php")
    fun cerrarRuta(
        @Query("id_ruta") id_ruta: String?,
        @Query("estatus") estatus: String?
    ): Call<String>

    @GET("puedeCerrarRuta.php")
    fun puedeCerrarRutaTarde(
        @Query("id_ruta") id_ruta: String?,
        @Query("estatus") estatus: String?
    ): Call<PuedeCerrarRutaResponse>

    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRutaTarde2.php")
    fun cerrarRutaTarde(
        @Field("id_ruta") id_ruta: String?,
        @Field("estatus") estatus: String?
    ): Call<String>

    //Postear recorrido
    //0->no es emergencia
    //1->es emergencia

    @GET("enviaRuta.php")
    fun enviarRuta(
        @Query("id_ruta") idRuta: String?, @Query("aux_id") aux_id: String?,
        @Query("latitud") latitud: String?, @Query("longitud") longitud: String?,
        @Query("es_emergencia") emergencia: String?,@Query("velocidad") velocidad: String?,
        @Query("accion") accion: String?="R",@Query("id_alumno") idAlumno: String?="0"
    ): Call<String>


    @GET("registrarCierreRuta.php")
    fun registrarCierreRuta(
        @Query("id_ruta") idRuta: String?,
        @Query("latitud") latitud: String?, @Query("longitud") longitud: String?,
        @Query("turno") turno: String?,@Query("android_id") android_id: String?
    ): Call<String>


    @GET("registrarMovimiento.php")
    fun registrarMovimiento(
        @Query("id_ruta") idRuta: String?,
        @Query("id_alumno") id_alumno: String?,
        @Query("android_id") android_id: String?,
        @Query("latitud") latitud: String?,
        @Query("longitud") longitud: String?,
        @Query("turno") turno: String?,
        @Query("movimiento") movimiento: String?,
    ): Call<String>




    @FormUrlEncoded
    @POST("crearSesion.php")
    fun crearSesion(
        @Field("usuario_id") usuario_id: String?,
        @Field("token") token: String?
    ): Call<String?>?


    //Procesar alumnos registrados offline
    @GET("procesarAlumnosMan.php")
    fun procesarMan(@Query("id_ruta") idRuta: String?,
                    @Query("id_alumno") idAlumno: String?,
                    @Query("ascenso") ascenso: String?,
                    @Query("descenso") descenso: String?,
                    @Query("hora") hora: String?):Call<String>

    @GET("procesarAlumnosTar.php")
    fun procesarTar(@Query("id_ruta") idRuta: String?,
                    @Query("id_alumno") idAlumno: String?,
                    @Query("ascenso") ascenso: String?,
                    @Query("descenso") descenso: String?,
                    @Query("hora") hora: String?):Call<String>


    //seguridad del dispositivo
    @FormUrlEncoded
    @POST("crearRegistroSeguridad2.php")
    fun registrarDispositivo(@Field("usuario_id") usuario_id: String?,
                             @Field("android_id") android_id:String?,
                             @Field("celular") celular:String?):Call<String>


    @GET("getDispositivoValido.php")
    fun validaDispositivo(@Query("android_id") android_id: String?,
                          @Query("usuario_id") usuario_id: String?):Call<DispositivoActivo>

    @GET("getDispositivoValido2.php")
    fun validaDispositivo2(@Query("android_id") android_id: String?):Call<DispositivoActivo>
}