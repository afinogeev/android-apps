package ru.afinogeev.moexrating.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.afinogeev.moexrating.util.Constants.Companion.BASE_URL

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: MoexRatingApi by lazy {
        retrofit.create(MoexRatingApi::class.java)
    }
}