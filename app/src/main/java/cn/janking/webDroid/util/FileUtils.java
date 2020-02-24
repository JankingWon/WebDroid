package cn.janking.webDroid.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @todo 添加文件权限
 */
public class FileUtils {
    static private String TAG = "FileUtils";
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

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    private static File getExistFile(String filePath) throws IOException {
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
            file.createNewFile();//有路径才能创建文件
        } else if(file.isFile()){
            file.delete();
            file.mkdir();
        }
        return file;
    }

    /**
     * 获取文件的名称，去除路径
     */
    public static String getFileName(String filePath){
        int index = filePath.lastIndexOf(File.separator);
        if (index < 0) return filePath;
        return filePath.substring(index + 1);
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

}
