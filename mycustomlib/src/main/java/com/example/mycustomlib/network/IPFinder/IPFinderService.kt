package com.example.mycustomlib.network.IPFinder

import retrofit2.Call
import retrofit2.http.GET

interface IPFinderService {

    @GET("ipdetails")
    fun getPublicIpAddress(): Call<IPFinderResponse>
}