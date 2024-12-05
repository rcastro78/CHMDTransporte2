package sv.com.chmd.transporte.networking

class TransporteAPI {
    companion object {
        private const val WEBSERVICE_TRANSPORTE_URL = "https://www.chmd.edu.mx/WebAdminCirculares/wsTransporte/"
        private const val WEBSERVICE_TRANSPORTE_SEGURIDAD_URL = "https://www.chmd.edu.mx/WebAdminCirculares/wsTransporteSeguro/"
        fun getCHMDService(): ITransporte? {
            return RetrofitClient.getClient(WEBSERVICE_TRANSPORTE_URL)?.create(ITransporte::class.java)
        }
    }
}