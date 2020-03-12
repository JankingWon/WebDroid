package cn.janking.webDroid.util

/**
 * @author Janking
 */
object ConstUtils {
    object SPKey{
        /**
         * 是否已经准备好打包apk
         */
        const val hasInit : String = "init"
        /**
         * 保存上次输入的配置
         */
        const val lastConfig : String = "lastConfig"
    }

    object Build{
        const val timeout : Long = 60 * 1000
    }
}