package sv.com.chmd.transporte.model

data class Asistencia(
    val tarjeta:String,
    val ascenso: String,
    val ascenso_t: String,
    val asistencia: String,
    val descenso: String,
    val descenso_t: String,
    val domicilio: String,
    val domicilio_s: String,
    val estatus: String,
    val fecha: String,
    val foto: String,
    val grado: String,
    val grupo: String,
    val hora_manana: String,
    val hora_regreso: String,
    val id_alumno: String,
    val id_ruta_h: String,
    val id_ruta_h_s: String,
    val nivel: String,
    val nombre: String,
    val orden_in: String?="0",
    val orden_out: String?="0",
    val salida: String,
    val tipo_asistencia: String,
    val orden_especial:String?="0"
)