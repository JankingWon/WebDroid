package cn.janking.webDroid.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @todo 添加文件权限
 */
public class FileUtil {
    /**
     * 复制目录
     *
     * @param fromDir
     * @param toDir
     * @throws IOException
     */
    public static void copyDir(String fromDir, String toDir) throws IOException {
        //创建目录的File对象
        File dirSouce = new File(fromDir);
        //判断源目录是不是一个目录
        if (!dirSouce.isDirectory()) {
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
        File[] files = dirSouce.listFiles();
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
                System.out.println("正在复制文件：" + file.getName());
                //递归调用复制文件的方法
                copyFile(strFrom, strTo);
            }
        }
    }

    /**
     * 复制文件
     *
     * @param fromFile
     * @param toFile
     * @throws IOException
     */
    public static void copyFile(String fromFile, String toFile) throws IOException {
        if(new File(fromFile).equals(getFile(toFile))){
            return;
        }
        //字节输入流——读取文件
        FileInputStream in = new FileInputStream(fromFile);
        copyFile(in, toFile);
    }

    /**
     * 复制文件
     *
     * @param in
     * @param toFile
     * @throws IOException out.close();
     */
    public static void copyFile(InputStream in, String toFile) throws IOException {
        //字节输出流——写入文件
        FileOutputStream out = new FileOutputStream(getFile(toFile));

        //把读取到的内容写入新文件
        //把字节数组设置大一些   1*1024*1024=1M
        byte[] bs = new byte[1 * 1024 * 1024];
        int count = 0;
        while ((count = in.read(bs)) != -1) {
            out.write(bs, 0, count);
        }
        //关闭流
        in.close();
        out.flush();
    }

    /**
     * 若不存在则创建
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static File getFile(String path) throws IOException {
        File file = new File(path);
        File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
        String fileParentPath = file.getParent();//返回的是String类型
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

    public static void copyAssets(Context context, String assetFolder) throws IOException {
        for (String name : context.getAssets().list(assetFolder)) {
            copyFile(context.getAssets().open(assetFolder + File.separator + name),
                    ConfigUtil.getPathRoot(assetFolder + File.separator + name));
        }
    }

}
