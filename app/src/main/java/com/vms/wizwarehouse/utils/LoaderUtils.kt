package com.vms.wizwarehouse.utils

import android.app.Dialog
import android.content.Context
import android.util.Log
import com.vms.wizwarehouse.R

object LoaderUtils {

    private var loaderDialog: Dialog? = null

    fun initLoader(context: Context) {
        loaderDialog = Dialog(context).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    fun showLoader() {
        if (loaderDialog != null && !loaderDialog!!.isShowing) {
            loaderDialog!!.show()
        }
    }

    fun hideLoader() {
        Log.d(
            "HIDE_LOADER",
            "loader ${loaderDialog != null} isShowing ${loaderDialog?.isShowing == true}"
        )

        if (loaderDialog != null && loaderDialog!!.isShowing) {
            loaderDialog!!.dismiss()
        }
    }

    fun isLoaderShowing(): Boolean {
        return loaderDialog != null && loaderDialog!!.isShowing
    }
}
