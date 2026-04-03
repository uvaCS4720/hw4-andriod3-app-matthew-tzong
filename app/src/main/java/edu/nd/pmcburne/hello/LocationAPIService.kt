package edu.nd.pmcburne.hello

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://www.cs.virginia.edu/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface LocationAPIService {
    @GET("~wxt4gm/placemarks.json")
    suspend fun getLocations(): List<Location>
}

object LocationAPI {
    val service: LocationAPIService by lazy {
        retrofit.create(LocationAPIService::class.java)
    }
}

data class Location(
    val id: Int,
    val name: String,
    @SerializedName("tag_list") val tagList: List<String>,
    val description: String,
    @SerializedName("visual_center") val visualCenter: VisualCenter
)

data class VisualCenter(
    val latitude: Double,
    val longitude: Double
)

