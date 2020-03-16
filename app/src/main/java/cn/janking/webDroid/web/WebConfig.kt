package cn.janking.webDroid.web

/**
 * @author Janking
 */
object WebConfig{
    /**
     * 打开文件页面的请求码
     */
    const val SELECT_FILE_REQUEST_CODE = 99
    /**
     * 直接打开其他页面
     */
    const val DIRECT_OPEN_OTHER_PAGE = 1001
    /**
     * 弹窗咨询用户是否前往其他页面
     */
    val ASK_USER_OPEN_OTHER_PAGE: Int = DIRECT_OPEN_OTHER_PAGE shr 2
    /**
     * 不允许打开其他页面
     */
    val DISALLOW_OPEN_OTHER_APP: Int = DIRECT_OPEN_OTHER_PAGE shr 4
    /**
     * 是否是debug模式
     */
    val DEBUG = true
    /**
     * 打开外部链接的方式
     */
    val handleOpenUrl = ASK_USER_OPEN_OTHER_PAGE
    /**
     * 是否拦截未知应用
     */
    val interceptUnknownUrl = true
    /**
     * 缩放
     */
    val abnormalScale = 7
}