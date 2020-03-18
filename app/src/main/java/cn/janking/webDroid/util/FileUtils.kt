package cn.janking.webDroid.util

import android.net.Uri
import android.os.Build
import cn.janking.webDroid.constant.PathConstants
import java.io.*
import java.util.*

object FileUtils {
    /**
     * Return whether the file exists.
     *
     * @param filePath The path of file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFileExists(filePath: String?): Boolean {
        return if (Build.VERSION.SDK_INT < 29) {
            isFileExists(
                getFileByPath(
                    filePath
                )
            )
        } else {
            try {
                val uri = Uri.parse(filePath)
                val cr =
                    Utils.getApp().contentResolver
                val afd =
                    cr.openAssetFileDescriptor(uri, "r") ?: return false
                try {
                    afd.close()
                } catch (ignore: IOException) {
                }
            } catch (e: FileNotFoundException) {
                return false
            }
            true
        }
    }

    /**
     * Return whether the file exists.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFileExists(file: File?): Boolean {
        return file != null && file.exists()
    }

    /**
     * 复制目录到目录
     *
     * @param fromDir 源目录
     * @param toDir   目的目录，源目录下所有文件和目录将直接复制到此目录
     */
    @Throws(IOException::class)
    fun copyDir(fromDir: String, toDir: String) { //创建目录的File对象
        val dirSource = File(fromDir)
        //判断源目录是不是一个目录
        if (!dirSource.isDirectory) { //如果不是目录那就不复制
            return
        }
        //创建目标目录的File对象
        val destDir = File(toDir)
        //如果目的目录不存在
        if (!destDir.exists()) { //创建目的目录
            destDir.mkdir()
        }
        //获取源目录下的File对象列表
        val files = dirSource.listFiles()
        for (file in files) { //拼接新的fromDir(fromFile)和toDir(toFile)的路径
            val strFrom = fromDir + File.separator + file.name
            println(strFrom)
            val strTo = toDir + File.separator + file.name
            println(strTo)
            //判断File对象是目录还是文件
            //判断是否是目录
            if (file.isDirectory) { //递归调用复制目录的方法
                copyDir(strFrom, strTo)
            }
            //判断是否是文件
            if (file.isFile) { //递归调用复制文件的方法
                copyFileToFile(strFrom, strTo)
            }
        }
    }

    /**
     * 复制文件到一个目录中
     */
    @Throws(IOException::class)
    fun copyFileToDir(fromFile: String, toDir: String) {
        copyFileToFile(
            fromFile,
            toDir + File.separator + getFileName(fromFile)
        )
    }

    /**
     * 复制文件到一个目录中
     */
    @Throws(IOException::class)
    fun copyFileToDir(fromFile: String, toDir: String, fileFormat: String) {
        copyFileToFile(
            fromFile,
            toDir + File.separator + getFileNameNoExtension(fromFile) + "." + fileFormat
        )
    }

    /**
     * 复制文件到一个目录中
     */
    @Throws(IOException::class)
    fun copyFileToDir(fromFile: File, toDir: String) {
        copyFileToFile(
            fromFile.absolutePath,
            toDir + File.separator + fromFile.name
        )
    }

    /**
     * 复制文件到一个目录中
     */
    @Throws(IOException::class)
    fun copyFileToDir(fromFile: File, toDir: String, fileFormat: String) {
        copyFileToFile(
            fromFile.absolutePath,
            toDir + File.separator + getFileNameNoExtension(fromFile) + "." + fileFormat
        )
    }

    /**
     * 复制文件到另一个文件中
     */
    @Throws(IOException::class)
    fun copyFileToFile(fromFile: String?, toFile: String?): Boolean {
        val existFile = getExistFile(toFile)
        val file = File(fromFile)
        if (file == getExistFile(toFile)) {
            return true
        }
        copyFileToFile(file, existFile)
        return true
    }

    /**
     * 复制文件到文件
     */
    @Throws(IOException::class)
    fun copyFileToFile(fromFile: File?, toFile: File) {
        try {
            FileInputStream(fromFile).use { inputStream ->
                FileOutputStream(
                    getExistFile(toFile)
                ).use { outputStream ->
                    copyFileToFile(
                        inputStream,
                        outputStream
                    )
                }
            }
        } finally {

        }
    }

    /**
     * 复制文件
     */
    @Throws(IOException::class)
    fun copyFileToFile(inputStream: InputStream, toFile: String?) {
        val outputStream = FileOutputStream(getExistFile(toFile))
        copyFileToFile(inputStream, outputStream)
    }

    /**
     * 复制文件到文件
     * 内部具体实现
     */
    @Throws(IOException::class)
    private fun copyFileToFile(
        fromFile: InputStream,
        toFile: OutputStream
    ) { //把读取到的内容写入新文件，把字节数组设置大一些   1*1024*1024=1M
        val bs = ByteArray(1 * 1024 * 1024)
        var count = 0
        while (fromFile.read(bs).also { count = it } != -1) {
            toFile.write(bs, 0, count)
        }
        //关闭流
        fromFile.close()
        toFile.flush()
    }


    /**
     * 复制uri到文件
     */
    @Throws(IOException::class)
    fun copyUriToFile(
        uri: Uri,
        toFile: File
    ) {
        copyFileToFile(
            UriUtils.uri2File(uri),
            getExistFile(toFile)
        )
    }

    /**
     * 复制uri到临时文件
     */
    @Throws(IOException::class)
    fun copyUriToTempFile(
        uri: Uri
    ): File {
        val outFile =
            getExistFile(
                PathConstants.dirTemp + File.separator + UUID.randomUUID().toString()
                    .substring(0, 5)
            )
        copyUriToFile(
            uri,
            outFile
        )
        return outFile
    }

    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    fun getFileByPath(filePath: String?): File? {
        return if (filePath.isNullOrBlank()) null else File(
            filePath
        )
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    @Throws(IOException::class)
    fun getExistFile(filePath: String?): File {
        return getExistFile(File(filePath))
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    @Throws(IOException::class)
    private fun getExistFile(file: File): File {
        val fileParent = file.parentFile //返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs() // 能创建多级目录
        }
        if (!file.exists()) {
            file.createNewFile() //有路径才能创建文件
        } else if (file.isDirectory) {
            file.delete()
            file.createNewFile()
        }
        return file
    }

    /**
     * 获取一个File类型的目录，如果不存在，尝试创建
     */
    @Throws(IOException::class)
    fun getExistDir(dirPath: String?): File {
        return getExistDir(File(dirPath))
    }

    /**
     * 获取一个File类型的目录，如果不存在，尝试创建
     */
    fun getExistDir(file: File): File {
        val fileParent = file.parentFile //返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs() // 能创建多级目录
        }
        if (!file.exists()) {
            file.mkdir() //有路径才能创建目录
        } else if (file.isFile) {
            file.delete()
            file.mkdir()
        }
        return file
    }

    /**
     * 获取文件的名称，去除路径
     */
    fun getFileName(filePath: String?): String {
        if (filePath.isNullOrBlank()) return ""
        val lastSep = filePath.lastIndexOf(File.separator)
        return if (lastSep == -1) filePath else filePath.substring(lastSep + 1)
    }

    /**
     * Return the extension of file.
     *
     * @param file The file.
     * @return the extension of file
     */
    fun getFileExtension(file: File?): String {
        return if (file == null) "" else getFileExtension(file.path)
    }

    /**
     * Return the extension of file.
     *
     * @param filePath The path of file.
     * @return the extension of file
     */
    fun getFileExtension(filePath: String?): String {
        if (filePath.isNullOrBlank()) return ""
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep = filePath.lastIndexOf(File.separator)
        return if (lastPoi == -1 || lastSep >= lastPoi) "" else filePath.substring(lastPoi + 1)
    }

    /**
     * Return the name of file without extension.
     *
     * @param file The file.
     * @return the name of file without extension
     */
    fun getFileNameNoExtension(file: File?): String? {
        return if (file == null) "" else getFileNameNoExtension(file.path)
    }

    /**
     * Return the name of file without extension.
     *
     * @param filePath The path of file.
     * @return the name of file without extension
     */
    fun getFileNameNoExtension(filePath: String?): String? {
        if (filePath.isNullOrBlank()) return ""
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep = filePath.lastIndexOf(File.separator)
        if (lastSep == -1) {
            return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
        }
        return if (lastPoi == -1 || lastSep > lastPoi) {
            filePath.substring(lastSep + 1)
        } else filePath.substring(lastSep + 1, lastPoi)
    }

    /**
     * 获取文件内容
     */
    fun getFileContent(filePath: String?): String? {
        val stringBuilder = StringBuilder()
        try {
            val `in` = BufferedReader(FileReader(filePath))
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                stringBuilder.append(str)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return stringBuilder.toString()
    }

    /**
     * 获取文件内容
     */
    fun getFileContent(inputStream: InputStream): String? {
        val stringBuilder = StringBuilder()
        try {
            val buf = ByteArray(1024)
            var length = 0
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
//当文件读取到结尾时返回 -1,循环结束。
            while (inputStream.read(buf).also { length = it } != -1) {
                stringBuilder.append(String(buf, 0, length))
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return stringBuilder.toString()
    }

    /**
     * Delete the directory.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun delete(filePath: String?): Boolean {
        return delete(
            getFileByPath(
                filePath
            )
        )
    }

    /**
     * Delete the directory.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun delete(file: File?): Boolean {
        if (file == null) return false
        return if (file.isDirectory) {
            deleteDir(file)
        } else deleteFile(file)
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.size != 0) {
            for (file in files) {
                if (file.isFile) {
                    if (!file.delete()) return false
                } else if (file.isDirectory) {
                    if (!deleteDir(file)) return false
                }
            }
        }
        return dir.delete()
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile && file.delete())
    }

    /**
     * Delete the all in directory.
     *
     * @param dirPath The path of directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteAllInDir(dirPath: String?): Boolean {
        return deleteAllInDir(
            getFileByPath(
                dirPath
            )
        )
    }

    /**
     * Delete the all in directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteAllInDir(dir: File?): Boolean {
        return deleteFilesInDirWithFilter(
            dir,
            FileFilter { true })
    }

    /**
     * Delete all files in directory.
     *
     * @param dirPath The path of directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDir(dirPath: String?): Boolean {
        return deleteFilesInDir(
            getFileByPath(
                dirPath
            )
        )
    }

    /**
     * Delete all files in directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDir(dir: File?): Boolean {
        return deleteFilesInDirWithFilter(
            dir,
            FileFilter { pathname -> pathname.isFile })
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dirPath The path of directory.
     * @param filter  The filter.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDirWithFilter(
        dirPath: String?,
        filter: FileFilter?
    ): Boolean {
        return deleteFilesInDirWithFilter(
            getFileByPath(
                dirPath
            ), filter
        )
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dir    The directory.
     * @param filter The filter.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDirWithFilter(
        dir: File?,
        filter: FileFilter?
    ): Boolean {
        if (dir == null || filter == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.size != 0) {
            for (file in files) {
                if (filter.accept(file)) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
        }
        return true
    }

    /**
     * 把字符串写入文件
     *
     * @param string
     * @param filePath
     */
    @Throws(IOException::class)
    fun writeToFile(string: String?, filePath: String?) {
        FileWriter(getExistFile(filePath))
            .use { writer ->
                writer.write(string)
                writer.flush()
            }
    }
}