package cn.janking.webDroid.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

/**
 * @author Janking
 */
object WebUtils{
    fun checkNetwork(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: return false
        @SuppressLint("MissingPermission") val info =
            connectivity.activeNetworkInfo
        return info != null && info.isConnected
    }
}