package com.vms.wizwarehouse.retrofit;


interface ApiResponseListener<T> {
    fun onSuccess(response: T)
    fun onFailure(error: String)
}