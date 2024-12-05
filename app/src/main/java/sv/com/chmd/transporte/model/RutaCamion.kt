package sv.com.chmd.transporte.model

class RutaCamion : ArrayList<RutaCamionItem>()

data class RutaCamionItem(
    val camion: String,
    val estatus: String,
    val id_ruta_h: String,
    val nombre_ruta: String,
    val tipo_ruta: String,
    val turno: String
)