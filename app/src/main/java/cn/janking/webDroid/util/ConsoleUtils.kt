package cn.janking.webDroid.util

import android.widget.TextView
import cn.janking.webDroid.R

/**
 * 可视化输出的工具类
 */
class ConsoleUtils {
    companion object {

        fun info(console: TextView?, message: String) {
            console?.let {
                SpanUtils.with(it)
                    .appendLine(message)
                    .create()
            }
        }

        /**
         * 保留原有输出添加info信息
         */
        fun infoAppend(console: TextView?, message: String) {
            console?.let {
                SpanUtils.with(it)
                    .append(it.text)
                    .appendLine(message)
                    .create()
            }
        }

        fun warning(console: TextView?, message: String) {
            console?.let {
                SpanUtils.with(it)
                    .appendLine(message)
                    .setForegroundColor(ColorUtils.getColor(R.color.rainbow_yellow))
                    .create()
            }
        }

        fun error(console: TextView?, message: String) {
            console?.let {
                SpanUtils.with(it)
                    .appendLine(message)
                    .setForegroundColor(ColorUtils.getColor(R.color.rainbow_red))
                    .create()
            }
        }

        fun success(console: TextView?, message: String) {
            console?.let {
                SpanUtils.with(it)
                    .appendLine(message)
                    .setForegroundColor(ColorUtils.getColor(R.color.loveGreen))
                    .create()
            }
        }
    }
}