package cn.janking.webDroid.helper

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Handler
import android.os.Message
import cn.janking.webDroid.R
import cn.janking.webDroid.util.*
import com.bumptech.glide.util.Util

/**
 */
object DialogHelper {

    /**
     * 提示用户允许权限
     */
    fun showRationaleDialog(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        AlertDialog.Builder(topActivity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage("You have rejected us to apply for authorization, please agree to authorization, otherwise the function can\\'t be used normally!")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                shouldRequest.again(true)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                shouldRequest.again(false)
                dialog.dismiss()
            }.show()
    }

    /**
     * 打开App权限设置页面
     */
    fun showOpenAppSettingDialog() {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        AlertDialog.Builder(topActivity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage("We need some of the permissions you rejected or the system failed to apply failed, please manually set to the page authorize, otherwise the function can\\'t be used normally!")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                PermissionUtils.launchAppDetailsSettings()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}
