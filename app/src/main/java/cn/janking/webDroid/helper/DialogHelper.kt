package cn.janking.webDroid.helper

import android.app.AlertDialog
import cn.janking.webDroid.util.ActivityUtils
import cn.janking.webDroid.util.PermissionUtils

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
        AlertDialog.Builder(topActivity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage("You have rejected us to apply for authorization, please agree to authorization, otherwise the function can\\'t be used normally!")
            .setPositiveButton(android.R.string.ok){
                    dialog, _ ->
                shouldRequest.again(true)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel){
                    dialog, _ ->
                shouldRequest.again(false)
                dialog.dismiss()
            }.show()
    }

    fun showOpenAppSettingDialog() {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        AlertDialog.Builder(topActivity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage("We need some of the permissions you rejected or the system failed to apply failed, please manually set to the page authorize, otherwise the function can\\'t be used normally!")
            .setPositiveButton(android.R.string.ok){
                    dialog, _ ->
                PermissionUtils.launchAppDetailsSettings()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel){
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}
