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
    }

    object Build{
        const val timeout : Long = 60 * 1000
    }
}