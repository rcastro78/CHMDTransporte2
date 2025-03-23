package sv.com.chmd.transporte.networking

import com.google.gson.Gson
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    @Volatile
    private var retrofit: Retrofit? = null
    private val gson: Gson = GsonBuilder().setLenient().create()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Tiempo de conexión más razonable
            .readTimeout(60, TimeUnit.SECONDS) // Tiempo de lectura óptimo
            .build()
    }

    fun getClient(url: String): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(url).also { retrofit = it }
        }
    }

    private fun buildRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
