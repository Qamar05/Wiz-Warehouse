package com.vms.wizwarehouse.otp

data class LoginRequest(
    var phone: String,
    var code: String
)

