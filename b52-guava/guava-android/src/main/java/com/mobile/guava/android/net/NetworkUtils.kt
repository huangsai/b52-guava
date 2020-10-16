package com.mobile.guava.android.net

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

const val NETWORK_TYPE_UNKNOWN = 0
const val NETWORK_TYPE_WIFI = 1
const val NETWORK_TYPE_2_G = 2
const val NETWORK_TYPE_3_G = 3
const val NETWORK_TYPE_4_G = 4
const val NETWORK_TYPE_5_G = 5

@SuppressWarnings("deprecation")
@SuppressLint("MissingPermission")
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    } else {
        connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}

fun operatorName(context: Context): String {
    val telephonyManager = context.getSystemService(
        Context.TELEPHONY_SERVICE
    ) as TelephonyManager
    return telephonyManager.simOperatorName
}

fun operatorNameZh(context: Context): String {
    val operatorName = operatorName(context).toLowerCase()
    return when {
        operatorName.contains("unicom", true) -> "中国联通"
        operatorName.contains("mobile", true) -> "中国移动"
        operatorName.contains("telecom", true) -> "中国电信"
        operatorName.contains("netcom", true) -> "中国网通"
        else -> operatorName
    }
}

@SuppressWarnings("deprecation")
@SuppressLint("MissingPermission")
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun networkType(context: Context): Int {
    val telephonyManager = context.getSystemService(
        Context.TELEPHONY_SERVICE
    ) as TelephonyManager

    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        telephonyManager.dataNetworkType
    } else {
        telephonyManager.networkType
    }
    return when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> NETWORK_TYPE_2_G
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP -> NETWORK_TYPE_3_G
        TelephonyManager.NETWORK_TYPE_LTE -> NETWORK_TYPE_4_G
        TelephonyManager.NETWORK_TYPE_NR -> NETWORK_TYPE_5_G
        else -> NETWORK_TYPE_UNKNOWN
    }
}

fun networkTypeName(networkClass: Int): String {
    return when (networkClass) {
        NETWORK_TYPE_WIFI -> "wifi"
        NETWORK_TYPE_2_G -> "2G"
        NETWORK_TYPE_3_G -> "3G"
        NETWORK_TYPE_4_G -> "4G"
        NETWORK_TYPE_5_G -> "5G"
        else -> "Unknown network class"
    }
}

