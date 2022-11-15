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
    // This function needs to be called from a coroutine, hence the suspend
    // in its type.  Also note the @Query annotation, which says that when
    // called, retrofit will add "&difficulty=%s".format(level) to the URL
    // Thanks, retrofit!
    // Hardcode several parameters in the GET for simplicity
    // So URL can have & and ? characters
    // XXX Write me: Retrofit annotation, see CatNet
    @Headers(
        "X-RapidAPI-Key: f8a22daf03msh26cfaf498ac6c85p1fcbd3jsn1d66675face4",
        "X-RapidAPI-Host: wordle-creator-tools.p.rapidapi.com"
    )
    @GET("/new-word?wordlength=5")
    suspend fun newWord() : NewWordle
    // XXX Write me: The return type
    //            .addHeader("X-RapidAPI-Key", "SIGN-UP-FOR-KEY")
//            .addHeader("X-RapidAPI-Host", "wordle-creator-tools.p.rapidapi.com")
//    @Headers({
//        "X-RapidAPI-Key: SIGN-UP-FOR-KEY",
//        "X-RapidAPI-Host: wordle-creator-tools.p.rapidapi.com"
//    })
    @Headers(
        "X-RapidAPI-Key: f8a22daf03msh26cfaf498ac6c85p1fcbd3jsn1d66675face4",
        "X-RapidAPI-Host: wordle-creator-tools.p.rapidapi.com"
    )
    @GET("/check-word")
    suspend fun checkWord(@Query("word") word: String) : CheckWordle

    companion object {
        // Leave this as a simple, base URL.  That way, we can have many different
        // functions (above) that access different "paths" on this server
        // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-http-url/
//        https://wordle-creator-tools.p.rapidapi.com/new-word?wordlength=5

        private var url = HttpUrl.Builder()
            .scheme("https")
            .host("wordle-creator-tools.p.rapidapi.com")
            .build()

        // Public create function that ties together building the base
        // URL and the private create function that initializes Retrofit
        fun create(): WordleApi = create(url)
        private fun create(httpUrl: HttpUrl): WordleApi {
//            val client = OkHttpClient()
//
//            val request = Request.Builder()
//                .url("https://wordle-creator-tools.p.rapidapi.com/check-word?word=speak")
//                .get()
//                .addHeader("X-RapidAPI-Key", "f8a22daf03msh26cfaf498ac6c85p1fcbd3jsn1d66675face4")
//                .addHeader("X-RapidAPI-Host", "wordle-creator-tools.p.rapidapi.com")
//                .build()

//            val response = client.newCall(request).execute()
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