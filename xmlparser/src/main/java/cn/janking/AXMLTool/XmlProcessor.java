package cn.janking.AXMLTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class XmlProcessor {
    public byte[] source;

    public XmlProcessor(String srcFilePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(srcFilePath));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        //ParserChunkUtils.xmlStruct.byteSrc = byteArrayOutputStream.toByteArray();
        fileInputStream.close();
        byteArrayOutputStream.close();
    }

    public void process(){

    }

}
