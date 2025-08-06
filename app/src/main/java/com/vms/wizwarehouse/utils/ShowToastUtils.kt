package com.vms.wizwarehouse.utils

import android.content.Context
import android.widget.Toast

object ShowToastUtils {

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
}
