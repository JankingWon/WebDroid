package cn.janking.webDroid.model

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * 用于webDroid的配置项
 */
class Config private constructor() {
    /**
     * 是否是预览模式
     */
    @Transient
    var preview = false
    /**
     * 是否全屏
     */
    var fullScreen = true
    /**
     * tab的标题集合
     */
    var titles: Array<String> = arrayOf("")
    /**
     * viewPager的url集合
     */
    var urls: Array<String> = arrayOf("https://github.com/JankingWon/WebDroid")
    /**
     * tab的个数
     */
    @Transient
    var tabCount = 0

    companion object {
        private val gson = Gson()
        /**
         * 单例
         */
        var instance: Config = Config()
            private set

        /**
         * 读取配置文件
         */
        @Throws(JsonSyntaxException::class)
        fun read(configString: String?) {
            instance = gson.fromJson(
                configString,
                Config::class.java
            ).apply {
                tabCount = titles.size.coerceAtMost(urls.size).let {
                    if (it in 0..1) 0 else it
                }
            }
        }
    }
}