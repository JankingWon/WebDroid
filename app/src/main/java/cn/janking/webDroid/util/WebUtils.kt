package cn.janking.webDroid.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.webkit.CookieManager

/**
 * @author Janking
 */
object WebUtils {
    /**
     * 检查网络状态
     */
    fun checkNetwork(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @SuppressLint("MissingPermission") val info =
            connectivity.activeNetworkInfo
        return info != null && info.isConnected
    }

    /**
     * 获取Cookie
     */
    fun getCookiesByUrl(url: String?): String? {
        return if (CookieManager.getInstance() == null) null else CookieManager.getInstance().getCookie(
            url
        )
    }

    /**
     * 检查网络类型
     */
    fun checkNetworkType(context: Context): Int {
        val netType = 0
        //连接管理对象
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo ?: return netType
        return when (networkInfo.type) {
            ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_WIMAX, ConnectivityManager.TYPE_ETHERNET -> 1
            ConnectivityManager.TYPE_MOBILE -> when (networkInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_EHRPD -> 2
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B -> 3
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> 4
                else -> netType
            }
            else -> netType
        }
    }
}