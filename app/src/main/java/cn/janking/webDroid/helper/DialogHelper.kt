package cn.janking.webDroid.helper

import android.util.Pair
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.janking.webDroid.R
import cn.janking.webDroid.dialog.CommonDialogContent
import cn.janking.webDroid.util.ActivityUtils
import cn.janking.webDroid.util.PermissionUtils
import cn.janking.webDroid.util.StringUtils

/**
 * ```
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/01/10
 * desc  : helper about dialog
 * ```
 */
object DialogHelper {

    fun showRationaleDialog(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        CommonDialogContent().init(topActivity as FragmentActivity?,
                StringUtils.getString(android.R.string.dialog_alert_title),
                StringUtils.getString(R.string.permission_rationale_message),
                Pair(StringUtils.getString(android.R.string.ok), View.OnClickListener {
                    shouldRequest.again(true)
                }),
                Pair(StringUtils.getString(android.R.string.cancel), View.OnClickListener {
                    shouldRequest.again(false)
                }))
                .show()
    }

    fun showOpenAppSettingDialog() {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        CommonDialogContent().init(topActivity as FragmentActivity?,
                StringUtils.getString(android.R.string.dialog_alert_title),
                StringUtils.getString(R.string.permission_denied_forever_message),
                Pair(StringUtils.getString(android.R.string.ok), View.OnClickListener {
                    PermissionUtils.launchAppDetailsSettings()
                }),
                Pair(StringUtils.getString(android.R.string.cancel), View.OnClickListener {
                }))
                .show()
    }
}
