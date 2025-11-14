package com.example.myapplication.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.myapplication.interfaces.NetworkChangeListener
import com.example.myapplication.utils.DialogEx.dialogCheckNetWork

class NetworkChangeReceiver(
    private val activity: Activity,
    private val networkChangeListener: NetworkChangeListener?
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showDialogCheckNetwork(activity)
    }

    private fun showDialogCheckNetwork(context: Context) {
        if (isNetworkAvailable(context)) {
            context.dialogCheckNetWork().dismiss()
            networkChangeListener?.onNetworkConnected()
        } else {
            context.dialogCheckNetWork(
                actionExit = { activity.finishAffinity() },
                actionTryAgain = { showDialogCheckNetwork(activity) }
            ).show()

        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}
