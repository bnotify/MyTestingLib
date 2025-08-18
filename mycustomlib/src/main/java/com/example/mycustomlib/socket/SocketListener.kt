package com.example.mycustomlib.socket

interface SocketListener {
    fun onConnect(args: Array<out Any>)
    fun onDisconnect()
    fun onConnectError(args: Array<out Any>)
    fun onMessageReceived(args: Array<out Any>)
    fun onRegisteredd(args: Array<out Any>)
}