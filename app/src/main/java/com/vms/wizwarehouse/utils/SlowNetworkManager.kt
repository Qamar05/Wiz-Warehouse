package com.vms.wizwarehouse.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SlowNetworkManager {

    private val slowNetworkDetected = MutableLiveData(false)

    fun getSlowNetworkLiveData(): LiveData<Boolean> = slowNetworkDetected

    fun notifySlowNetwork() {
        slowNetworkDetected.postValue(true)
    }

    fun reset() {
        slowNetworkDetected.postValue(false)
    }
}
