package com.example.dynamicwallpaper.Common

sealed class NetworkResult<out T>{
    data class Success<out T>(val data:T) : NetworkResult<T>()
    data class Error(val message:String,val code:Int) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}