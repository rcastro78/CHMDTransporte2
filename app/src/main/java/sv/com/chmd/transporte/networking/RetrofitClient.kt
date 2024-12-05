package sv.com.chmd.transporte.networking

import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object{
        private var retrofit: Retrofit? = null
        fun getClient(url: String?): Retrofit? {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            if (retrofit == null) {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS) // Tiempo máximo para establecer la conexión
                    .readTimeout(300, TimeUnit.SECONDS) // Tiempo máximo para leer datos
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create()) //important
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit
        }
    }
}