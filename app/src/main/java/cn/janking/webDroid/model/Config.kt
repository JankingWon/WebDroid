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
     * APP的名称
     */
    var appName = ""

    /**
     * APP包名
     */
    var appPackage = ""

    /**
     * 是否显示标题栏
     */
    var showActionBar = true
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
    /**
     * tab的风格
     * 0 : top
     * 1 : bottom
     */
    @Transient
    var tabStyle = 0

    companion object {
        private val gson = Gson()
        /**
         * 单例
         */
        var instance: Config = Config()
            private set

        /**
         * 读取json字符串
         */
        @Throws(JsonSyntaxException::class)
        fun readFromString(configString: String?) {
            instance = gson.fromJson(
                configString,
                Config::class.java
            ).apply {
                tabCount = titles.size.coerceAtMost(urls.size).let {
                    if (it in 0..1) 0 else it
                }
            }
        }

        /**
         * 生成json配置文件
         */
        fun generateJson() : String {
            return gson.toJson(instance)
        }
    }
}