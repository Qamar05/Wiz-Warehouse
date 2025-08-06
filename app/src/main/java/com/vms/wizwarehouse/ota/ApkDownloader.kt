package com.vms.wizwarehouse.ota

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

object ApkDownloader {
    private var progressDialog: ProgressDialog? = null
    private var downloadId: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    fun downloadAndInstallApk(context: Context, apkUrl: String) {
        showProgressDialog(context)

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Downloading Update")
            .setDescription("Please wait while the update is downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)

        trackDownloadProgress(context, manager)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    progressDialog?.dismiss()
                    Toast.makeText(ctx, "Update downloaded.", Toast.LENGTH_LONG).show()
                    installApkIfPossible(ctx)
                }
            }
        }

        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun showProgressDialog(context: Context) {
        progressDialog = ProgressDialog(context).apply {
            setTitle("Downloading Update")
            setMessage("Please wait...\nKindly uninstall the app and install the updated version.")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            isIndeterminate = false
            max = 100
            setCancelable(false)
            show()
        }
    }

//    private fun trackDownloadProgress(context: Context, manager: DownloadManager) {
//        Thread {
//            var downloading = true
//            while (downloading) {
//                val query = DownloadManager.Query().setFilterById(downloadId)
//                val cursor: Cursor? = manager.query(query)
//                cursor?.use {
//                    if (it.moveToFirst()) {
//                        val bytesDownloaded =
//                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                        val bytesTotal =
//                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                        if (bytesTotal > 0) {
//                            val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
//                            handler.post { progressDialog?.progress = progress }
//                        }
//
//                        when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
//                            DownloadManager.STATUS_SUCCESSFUL -> downloading = false
//                            DownloadManager.STATUS_FAILED -> {
//                                downloading = false
//                                handler.post {
//                                    progressDialog?.dismiss()
//                                    Toast.makeText(context, "Download failed", Toast.LENGTH_LONG)
//                                        .show()
//                                }
//                            }
//                        }
//                    }
//                }
//                Thread.sleep(500)
//            }
//        }.start()
//    }

    private fun trackDownloadProgress(context: Context, manager: DownloadManager) {
        Thread {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor? = manager.query(query)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val bytesDownloaded =
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val bytesTotal =
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (bytesTotal > 0) {
                            val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                            handler.post {
                                progressDialog?.progress = progress
                                if (progress == 100) {
                                    progressDialog?.dismiss()
                                }
                            }
                        }

                        when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_SUCCESSFUL -> downloading = false
                            DownloadManager.STATUS_FAILED -> {
                                downloading = false
                                handler.post {
                                    progressDialog?.dismiss()
                                    Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                Thread.sleep(500)
            }
        }.start()
    }


    private fun installApkIfPossible(context: Context) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )
        if (!file.exists()) {
            Toast.makeText(context, "APK not found", Toast.LENGTH_LONG).show()
            return
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Installation failed", Toast.LENGTH_LONG).show()
        }
    }
}

