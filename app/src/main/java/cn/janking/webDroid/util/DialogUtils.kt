package cn.janking.webDroid.util

import android.app.AlertDialog
import cn.janking.webDroid.R

/**
 * @author Janking
 */
object DialogUtils {

    fun showAlertDialog(
        message: Int,
        positiveListener: Runnable,
        negativeListener: Runnable? = null
    ) {
        showAlertDialog(
            Utils.getApp().resources.getString(message),
            positiveListener,
            negativeListener
        )
    }

    /**
     * 统一显示dialog
     */
    fun showAlertDialog(
        message: String,
        positiveListener: Runnable,
        negativeListener: Runnable? = null
    ) {
        AlertDialog.Builder(ActivityUtils.getTopActivity())
            .setTitle(R.string.msg_note)
            .setMessage(
                message
            )
            .setNegativeButton(
                android.R.string.cancel
            ) { _, _ ->
                negativeListener?.run()
            }
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                positiveListener.run()
            }
            .show()
    }

}