package cn.janking.webDroid.web;

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import java.util.*

/**
 * @author Janking
 */
class WebVideoPlayer(private val activity: Activity, private val webViw: WebView?) {
    private var mFlags: MutableSet<Pair<Int?, Int>> = HashSet()
    private var videoView: View? = null
    private var videoLayout: ViewGroup? = null
    private var mCallback: WebChromeClient.CustomViewCallback? = null
    init {
        mFlags = HashSet()
    }

    /**
     * 播放视频
     */
    fun onShowCustomView(
        view: View?,
        callback: WebChromeClient.CustomViewCallback?
    ) {
        //表示正在显示自定义View
        if (videoView != null) {
            callback?.onCustomViewHidden()
            return
        }
        //切换全屏
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val mWindow = activity.window
        var mPair: Pair<Int?, Int>?
        // 保存当前屏幕的状态
        if (mWindow.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON == 0) {
            mPair = Pair(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                0
            )
            mWindow.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
            mFlags.add(mPair)
        }
        mPair = Pair(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            0
        )
        mWindow.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        mFlags.add(mPair)
        webViw?.visibility = View.GONE
        //添加视频布局
        if (videoLayout == null) {
            val mDecorView =
                activity.window.decorView as FrameLayout
            mDecorView.addView(FrameLayout(activity).also {
                it.setBackgroundColor(Color.BLACK)
                videoLayout = it
            })
        }
        mCallback = callback
        videoLayout!!.addView(view.also { videoView = it })
        videoLayout!!.visibility = View.VISIBLE
    }


    /**
     * 退出全屏播放
     */
    fun onHideCustomView() {
        if (videoView == null) {
            return
        }
        //取消全屏
        if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        //恢复flag
        if (mFlags.isNotEmpty()) {
            for (mPair in mFlags) {
                activity.window.setFlags(mPair.second, mPair.first!!)
            }
            mFlags.clear()
        }
        //去掉视频的View
        videoView!!.visibility = View.GONE
        videoLayout?.also {
            it.removeView(videoView)
            it.visibility = View.GONE
        }
        mCallback?.onCustomViewHidden()
        videoView = null
        webViw?.visibility = View.VISIBLE
    }


    /**
     * 处理返回事件
     */
    fun handleKeyEvent(): Boolean {
        return if (videoView != null) {
            //退出全屏
            onHideCustomView()
            true
        } else {
            false
        }
    }

}
