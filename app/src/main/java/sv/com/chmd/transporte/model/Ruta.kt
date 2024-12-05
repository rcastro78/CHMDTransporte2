package sv.com.chmd.transporte.model


data class Ruta(
    val auxiliar: String,
    val camion: String,
    val cupos: String,
    val id_ruta: String,
    val nombre_ruta: String,
    val tipo_ruta: String,
    val turno: String,
    val estatus:String
)