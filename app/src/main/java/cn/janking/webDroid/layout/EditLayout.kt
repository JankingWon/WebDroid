package cn.janking.webDroid.layout

import android.view.View

/**
 * @author Janking
 */
abstract class EditLayout {
    abstract val contentView : View

    abstract fun loadConfig()

    abstract fun generateConfig()
}