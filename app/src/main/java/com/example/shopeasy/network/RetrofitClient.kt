package com.example.shopeasy.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    /**
     * URL de la API .NET. 
     * Si estaba funcionando en la nube, asegúrate de poner aquí la URL de Google Cloud (ej. https://tu-app.a.run.app/api/)
     */
    private const val BASE_URL = "https://192.168.1.8:7276/api/"

    private var retrofit: Retrofit? = null

    fun getInstance(context: Context): ApiService {
        Log.d("RetrofitClient", "Using BASE_URL: $BASE_URL")
        if (retrofit == null) {
            val tokenManager = TokenManager(context)
            
            val logging = HttpLoggingInterceptor { message ->
                Log.d("API_DEBUG", message)
            }
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            // BYPASS SSL VALIDATION (Required for local .NET dev certificates)
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(tokenManager))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}
