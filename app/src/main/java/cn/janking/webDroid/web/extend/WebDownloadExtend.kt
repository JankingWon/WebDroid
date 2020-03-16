package cn.janking.webDroid.web.extend

import android.net.Uri
import android.webkit.DownloadListener
import android.webkit.WebView
import android.widget.Toast
import cn.janking.webDroid.R
import cn.janking.webDroid.helper.PermissionHelper
import cn.janking.webDroid.util.DialogUtils
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.Utils
import cn.janking.webDroid.util.WebUtils
import cn.janking.webDroid.web.WebConfig
import com.download.library.*
import com.download.library.DownloadListenerAdapter
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Janking
 * 设置下载器
 */
fun WebView.defaultDownloadListener() {
    setDownloadListener(DefaultDownloadImpl())
}

open class DefaultDownloadImpl : DownloadListener {
    /**
     * 保存下载请求
     */
    protected var mDownloadTasks =
        ConcurrentHashMap<String, ResourceRequest<*>>()
    /**
     * 重载DownloadListener，下载任务开始时调用
     */
    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        val resourceRequest = DownloadImpl
            .getInstance()
            .with(Utils.getApp())
            .url(url)
            .setEnableIndicator(true)
            .autoOpenIgnoreMD5()
        mDownloadTasks[url] = resourceRequest
        PermissionHelper.checkStorage(Runnable {
            determineDownload(url)
        })
    }


    /**
     * 判断网络
     */
    private fun determineDownload(url: String) {
        // 移动数据
        if (WebUtils.checkNetworkType(Utils.getApp()) > 1) {
            DialogUtils.showAlertDialog(
                R.string.msg_use_cellular_network,
                Runnable {
                    performDownload(url)
                }
            )
            return
        }else{
            performDownload(url)
        }
    }


    /**
     * 执行下载
     */
    private fun performDownload(url: String) {
        try {
            LogUtils.i(
                "download:$url",
                "exist:" + DownloadImpl.getInstance().exist(url)
            )
            // 该链接是否正在下载
            if (DownloadImpl.getInstance().exist(url)) {
                Toast.makeText(Utils.getApp(), "该文件正在下载！", Toast.LENGTH_SHORT).show()
                return
            }
            mDownloadTasks[url]?.run {
                //添加当前url的Cookie
                addHeader("Cookie", WebUtils.getCookiesByUrl(url))
                //推入队列
                enqueue(object : DownloadListenerAdapter() {
                    override fun onResult(
                        throwable: Throwable?,
                        path: Uri?,
                        url: String?,
                        extra: Extra?
                    ): Boolean {
                        mDownloadTasks.remove(url)
                        return super.onResult(throwable, path, url, extra)
                    }
                })
            }
        } catch (ignore: Throwable) {
            if (WebConfig.DEBUG) {
                ignore.printStackTrace()
            }
        }
    }

}