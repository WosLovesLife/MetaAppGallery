package com.example.metaappgallery.utils

import android.accounts.NetworkErrorException
import com.example.metaappgallery.repository.RefreshGalleryError
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.TimeoutCancellationException
import org.apache.http.conn.ConnectTimeoutException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

fun Throwable.formatMessage(): String {
    if (this.isNetworkProblem()) {
        return "无法连接到网络，请检查您的网络"
    }
    if (this is TimeoutCancellationException) {
        return "请求超时, 请稍后重试"
    }
    val message = message
    return when (this) {
        is JsonSyntaxException -> "数据解析错误"
        is RefreshGalleryError -> message ?: "未能获取到数据"
        else -> toString()
    }
}

fun Throwable.isNetworkProblem(): Boolean {
    if (this is UnknownHostException
        || this is ConnectException
        || this is SocketTimeoutException
        || this is SocketException
        || this is ConnectTimeoutException
        || this is TimeoutException
        || this is NetworkErrorException
        || this is HttpException
    ) {
        return true
    }
    if (this is IOException) {
        val message = message
        if (message != null && message.contains("reponse's code is")) {
            val responseCode: Int = try {
                val substring = message.substring(message.length - 3)
                Integer.parseInt(substring)
            } catch (e2: Exception) {
                0 // 解析错误,抓到未知异常
            }
            if (responseCode == 408) {
                return true
            }
        }
    }
    return false
}