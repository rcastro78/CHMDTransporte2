package sv.com.chmd.transporte.util

sealed class EstadoDispositivo {
    object Valido : EstadoDispositivo()
    object Registrado : EstadoDispositivo()
    object NoRegistrado : EstadoDispositivo()
    object NoValido : EstadoDispositivo()
    data class Desconocido(val valor: String) : EstadoDispositivo()

    companion object {
        fun getCodigoEstado(codigo: String): EstadoDispositivo {
            return when (codigo) {
                "granted" -> Valido
                "-1" -> Registrado
                "-2" -> NoRegistrado
                "denied" -> NoValido
                else -> Desconocido(codigo)
            }
        }
    }
}