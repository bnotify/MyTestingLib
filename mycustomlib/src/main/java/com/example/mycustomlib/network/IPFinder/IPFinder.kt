package com.example.mycustomlib.network.IPFinder

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IPFinder private constructor(private val context: Context, private val type: IPFinderClass, private val ipListener: IPListener) {

    private val connectionObserver: ConnectionLiveData = ConnectionLiveData(context)

    private val ipFinderService: IPFinderService = getIpfyService(type)

    private val ipAddressData = IPAddressData(null, null)

    private val ipAddressLiveData = MutableLiveData<IPAddressData>()

    init {
        connectionObserver.observeForever { isConnectedToInternet ->
            if (isConnectedToInternet) {
                callIpifyForCurrentIP()
            } else {
                resetCurrentIp()
            }
        }
    }

    private fun resetCurrentIp() {
        ipAddressData.lastStoredIpAddress = ipAddressData.currentIpAddress
        ipAddressData.currentIpAddress = null
        ipAddressLiveData.value = ipAddressData
    }

    private fun callIpifyForCurrentIP() {
        ipFinderService.getPublicIpAddress().enqueue(object : Callback<IPFinderResponse> {
            override fun onResponse(call: Call<IPFinderResponse>, response: Response<IPFinderResponse>) {
                val ipResponse = response.body()
                if (response.isSuccessful && ipResponse != null) {
                    ipListener.onSuccess(ipResponse)
                    ipAddressData.lastStoredIpAddress = ipAddressData.currentIpAddress
                    ipAddressData.currentIpAddress = ipResponse.data?.ip_data?.ip_address
                    ipAddressLiveData.value = ipAddressData

                    Log.d("IP_DATA_INFO", "${response.raw()}")
                } else {
                    resetCurrentIp()
                }
            }

            override fun onFailure(call: Call<IPFinderResponse>, t: Throwable) {
                resetCurrentIp()
                ipListener.onError(t.message.toString())
            }
        })
    }

    fun getPublicIpObserver(): MutableLiveData<IPAddressData> {
        return ipAddressLiveData
    }

    companion object{

        private const val UNIVERSAL_IP_BASE_URL = "https://ffmpegimages.mjunoon.tv/geolocation/public/api/"
        private const val IPv4_BASE_URL = "https://ffmpegimages.mjunoon.tv/geolocation/public/api/"

        private var ipListener: IPListener? = null

        @SuppressLint("StaticFieldLeak")
        private var instance: IPFinder? = null

        fun init(context: Context, type: IPFinderClass = IPFinderClass.IPv4, listener: IPListener): IPFinder {
            this.ipListener = listener
            val newInstance = IPFinder(context, type, listener)
            instance = newInstance
            return newInstance
        }

        @JvmName("getInstance1")
        fun getInstance(): IPFinder {
            instance?.let {
                return it
            } ?: run {
                throw Exception("Ipfy.init() with application context and type must be called to initialize Ipfy")
            }
        }



    }

    private fun getIpfyService(type: IPFinderClass): IPFinderService {
        val retrofit = Retrofit.Builder()
            .baseUrl(if (type == IPFinderClass.UniversalIP) UNIVERSAL_IP_BASE_URL else IPv4_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(IPFinderService::class.java)
    }

    interface IPListener{
        fun onSuccess(ipFinderResponse: IPFinderResponse)
        fun onError(error: String)
    }
}