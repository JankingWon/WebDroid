package cn.janking.webDroid.web

import android.net.Uri

/**
 * @author Janking
 */
interface FilePathChooserCallback {
    fun onChooseFile(uris: Array<Uri>)
}