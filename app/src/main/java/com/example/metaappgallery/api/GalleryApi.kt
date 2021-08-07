package com.example.metaappgallery.api

import com.example.metaappgallery.pojo.PagingResponse
import com.example.metaappgallery.pojo.PictureData
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GalleryApi {

    @GET("j")
    suspend fun getPictures(
        @Query("q") q: String,
        @Query("sn") sn: Int,
        @Query("pn") pn: Int
    ): PagingResponse<PictureData>

    companion object {
        private const val BASE_URL = "http://image.so.com/"

        fun create(): GalleryApi {
            val client = OkHttpClient.Builder()
                .build()
            return Retrofit.Builder()
                .baseUrl(HttpUrl.parse(BASE_URL)!!)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GalleryApi::class.java)
        }
    }
}