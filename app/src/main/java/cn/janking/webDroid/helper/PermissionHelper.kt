package cn.janking.webDroid.helper

import cn.janking.webDroid.constant.PermissionConstants
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.PermissionUtils
import kotlinx.android.synthetic.main.activity_creator.*

/**
 * ```
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/01/06
 * desc  : helper about permission
 * ```
 */
object PermissionHelper {
    /**
     * 检查是否有存储权限
     */
    fun checkStorage(
        grantedListener: () -> Unit,
        deniedListener: () -> Unit
    ) {
        checkPermission(PermissionConstants.STORAGE, grantedListener, deniedListener)
    }

    private fun checkPermission(
        @PermissionConstants.Permission permission: String,
        grantedListener: () -> Unit,
        deniedListener: () -> Unit
    ) {
        //之所以要多多余这个判断是PermissionUtils会调用透明请求的Activity，导致屏幕闪烁
        if(PermissionUtils.isGranted(PermissionConstants.STORAGE)){
            grantedListener()
            return
        }
        PermissionUtils.permission(permission)
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    LogUtils.d(permissionsGranted)
                    grantedListener()
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    LogUtils.d(permissionsDeniedForever, permissionsDenied)
                    //如果选择了“拒绝后不再询问”，则引导打开权限设置页面
//                    if (permissionsDeniedForever.isNotEmpty()) {
//                        DialogHelper.showOpenAppSettingDialog()
//                        return
//                    }
                }
            })
            .request()
    }
}
