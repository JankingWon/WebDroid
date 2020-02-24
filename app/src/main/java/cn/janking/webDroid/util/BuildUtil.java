package cn.janking.webDroid.util;

import java.io.IOException;
import java.io.InputStream;

public class BuildUtil {


    /*执行一个shell命令，并返回字符串值
     *
     * @param cmd
     * 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
     * @param workdirectory
     * 命令执行路径（例如："system/bin/"）
     * @return 执行结果组成的字符串
     * @throws IOException
     */
    public static synchronized String runshell(String[] cmd)
            throws IOException
    {
        StringBuffer result = new StringBuffer();
        try
        {
            // 创建操作系统进程（也可以由Runtime.exec()启动）
            // Runtime runtime = Runtime.getRuntime();
            // Process proc = runtime.exec(cmd);
            // InputStream inputstream = proc.getInputStream();
            ProcessBuilder builder = new ProcessBuilder(cmd);
            InputStream in = null;
            // 设置一个路径（绝对路径了就不一定需要）
            // 设置工作目录（同上）
            // builder.directory(workPath);
            // 合并标准错误和标准输出
            builder.redirectErrorStream(true);
            // 启动一个新进程
            Process process = builder.start();
            // 读取进程标准输出流
            in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1)
            {
                result = result.append(new String(re));
            }

            // 关闭输入流
            if (in != null)
            {
                in.close();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result.toString();
    }
}
