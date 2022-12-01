package edu.cs371m.wordle.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface WordleApi {
    @Headers(
        "X-RapidAPI-Key: f8a22daf03msh26cfaf498ac6c85p1fcbd3jsn1d66675face4",
        "X-RapidAPI-Host: wordle-creator-tools.p.rapidapi.com"
    )
    @GET("/new-word?")
    suspend fun newWord(@Query("wordlength") length: Int) : NewWordle
    @Headers(
        "X-RapidAPI-Key: f8a22daf03msh26cfaf498ac6c85p1fcbd3jsn1d66675face4",
        "X-RapidAPI-Host: wordle-creator-tools.p.rapidapi.com"
    )
    @GET("/check-word")
    suspend fun checkWord(@Query("word") word: String) : CheckWordle

    companion object {
        private var url = HttpUrl.Builder()
            .scheme("https")
            .host("wordle-creator-tools.p.rapidapi.com")
            .build()

        fun create(): WordleApi = create(url)
        private fun create(httpUrl: HttpUrl): WordleApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WordleApi::class.java)
        }
    }
}