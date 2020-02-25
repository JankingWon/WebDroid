package cn.janking.webDroid.util;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileUtils {

    /**
     * Return whether the file exists.
     *
     * @param filePath The path of file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final String filePath) {
        if (Build.VERSION.SDK_INT < 29) {
            return isFileExists(getFileByPath(filePath));
        } else {
            try {
                Uri uri = Uri.parse(filePath);
                ContentResolver cr = Utils.getApp().getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) return false;
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
    }

    /**
     * Return whether the file exists.
     *
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }
    /**
     * 复制目录到目录
     * @param fromDir 源目录
     * @param toDir 目的目录，源目录下所有文件和目录将直接复制到此目录
     */
    public static void copyDir(String fromDir, String toDir) throws IOException {
        //创建目录的File对象
        File dirSource = new File(fromDir);
        //判断源目录是不是一个目录
        if (!dirSource.isDirectory()) {
            //如果不是目录那就不复制
            return;
        }
        //创建目标目录的File对象
        File destDir = new File(toDir);
        //如果目的目录不存在
        if (!destDir.exists()) {
            //创建目的目录
            destDir.mkdir();
        }
        //获取源目录下的File对象列表
        File[] files = dirSource.listFiles();
        for (File file : files) {
            //拼接新的fromDir(fromFile)和toDir(toFile)的路径
            String strFrom = fromDir + File.separator + file.getName();
            System.out.println(strFrom);
            String strTo = toDir + File.separator + file.getName();
            System.out.println(strTo);
            //判断File对象是目录还是文件
            //判断是否是目录
            if (file.isDirectory()) {
                //递归调用复制目录的方法
                copyDir(strFrom, strTo);
            }
            //判断是否是文件
            if (file.isFile()) {
                //递归调用复制文件的方法
                copyFileToFile(strFrom, strTo);
            }
        }
    }

    /**
     * 复制文件到一个目录中
     */
    public static boolean copyFileToDir(String fromFile, String toPath) {
        return copyFileToFile(fromFile, toPath + File.separator + fromFile.substring(fromFile.lastIndexOf(File.separator)));
    }

    /**
     * 复制文件到另一个文件中
     */
    public static boolean copyFileToFile(String fromFile, String toFile) {
        try {
            File existFile = getExistFile(toFile);
            File file= new File(fromFile);
            if(file.equals(getExistFile(toFile))){
                return true;
            }
            copyFileToFile(file, existFile);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 复制文件到文件
     */
    public static boolean copyFileToFile(File fromFile, File toFile){
        FileInputStream inputStream;
        FileOutputStream outputStream;
        try {
            inputStream = new FileInputStream(fromFile);
            outputStream = new FileOutputStream(getExistFile(toFile));
            copyFileToFile(inputStream, outputStream);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 复制文件
     */
    public static void copyFileToFile(InputStream inputStream, String toFile) throws IOException {
        FileOutputStream outputStream;
        outputStream = new FileOutputStream(getExistFile(toFile));
        copyFileToFile(inputStream, outputStream);
    }

    /**
     * 复制文件到文件
     * 内部具体实现
     */
    private static void copyFileToFile(InputStream fromFile, OutputStream toFile) throws IOException{
        //把读取到的内容写入新文件
        //把字节数组设置大一些   1*1024*1024=1M
        byte[] bs = new byte[1 * 1024 * 1024];
        int count = 0;
        while ((count = fromFile.read(bs)) != -1) {
            toFile.write(bs, 0, count);
        }
        //关闭流
        fromFile.close();
        toFile.flush();
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    public static File getExistFile(String filePath) throws IOException {
        return getExistFile(new File(filePath));
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    private static File getExistFile(File file) throws IOException {
        File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!file.exists()) {
            file.createNewFile();//有路径才能创建文件
        } else if(file.isDirectory()){
            file.delete();
            file.createNewFile();
        }
        return file;
    }

    /**
     * 获取一个File类型的目录，如果不存在，尝试创建
     */
    public static File getExistDir(String dirPath) throws IOException {
        return getExistDir(new File(dirPath));
    }

    /**
     * 获取一个File类型的目录，如果不存在，尝试创建
     */
    private static File getExistDir(File file) throws IOException {
        File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!file.exists()) {
            file.mkdir();//有路径才能创建目录
        } else if(file.isFile()){
            file.delete();
            file.mkdir();
        }
        return file;
    }

    /**
     * 获取文件的名称，去除路径
     */
    public static String getFileName(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
    }

    /**
     * Return the extension of file.
     *
     * @param file The file.
     * @return the extension of file
     */
    public static String getFileExtension(final File file) {
        if (file == null) return "";
        return getFileExtension(file.getPath());
    }

    /**
     * Return the extension of file.
     *
     * @param filePath The path of file.
     * @return the extension of file
     */
    public static String getFileExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

    /**
     * 获取文件内容
     */
    public static String getFileContent(String filePath){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String str;
            while ((str = in.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stringBuilder.toString();
    }

    /**
     * 获取文件内容
     */
    public static String getFileContent(InputStream inputStream){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] buf = new byte[1024];
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            while((length = inputStream.read(buf)) != -1){
                stringBuilder.append(new String(buf,0,length));
            }
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return stringBuilder.toString();
    }

    /**
     * Delete the directory.
     *
     * @param filePath The path of file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean delete(final String filePath) {
        return delete(getFileByPath(filePath));
    }

    /**
     * Delete the directory.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean delete(final File file) {
        if (file == null) return false;
        if (file.isDirectory()) {
            return deleteDir(file);
        }
        return deleteFile(file);
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteDir(final File dir) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    /**
     * Delete the all in directory.
     *
     * @param dirPath The path of directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteAllInDir(final String dirPath) {
        return deleteAllInDir(getFileByPath(dirPath));
    }

    /**
     * Delete the all in directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteAllInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        });
    }

    /**
     * Delete all files in directory.
     *
     * @param dirPath The path of directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDir(final String dirPath) {
        return deleteFilesInDir(getFileByPath(dirPath));
    }

    /**
     * Delete all files in directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dirPath The path of directory.
     * @param filter  The filter.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDirWithFilter(final String dirPath,
                                                     final FileFilter filter) {
        return deleteFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dir    The directory.
     * @param filter The filter.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDirWithFilter(final File dir, final FileFilter filter) {
        if (dir == null || filter == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    if (file.isFile()) {
                        if (!file.delete()) return false;
                    } else if (file.isDirectory()) {
                        if (!deleteDir(file)) return false;
                    }
                }
            }
        }
        return true;
    }

}
