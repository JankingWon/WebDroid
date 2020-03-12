package cn.janking.binaryXml.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import luyao.parser.parser.xml.XmlParser;


public class ManifestUtils {
    private String filePath;
    private XmlParser xmlParser;


    public ManifestUtils(String srcFilePath, String destFilePath) throws IOException {
        if (destFilePath == null) {
            filePath = srcFilePath;
        } else {
            filePath = destFilePath;
        }
        try (FileInputStream fileInputStream = new FileInputStream(new File(srcFilePath));
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            xmlParser = new XmlParser(byteArrayOutputStream.toByteArray());
        } finally {

        }
    }

    public void exec() throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath))) {
            fileOutputStream.write(xmlParser.reader.data);
        } finally {

        }
    }

    public ManifestUtils check() throws UnsupportedEncodingException {
        xmlParser.parse();
        return this;
    }

    public ManifestUtils modifyStringAttribute(String oldAttr, String newAttr) throws IOException {
        if (oldAttr.equals(newAttr)) {
            return this;
        }
        xmlParser.parse();
        int attrValueIndex = xmlParser.stringChunkList.indexOf(oldAttr);
        if (attrValueIndex == -1) {
            throw new RuntimeException("找不到该属性值[old:" + oldAttr + "][new:" + newAttr + "]");
        }
        xmlParser.stringChunkList.set(attrValueIndex, newAttr);
        //如果字符串长度一样，只需要修改StringPool中的原来字符串即可
        //magicNumber和fileSize
        int widthHead = 4 * 2;
        //StringChunk中 ChunkType 到StylePoolOffset
        int widthStringTrunkHead = 4 * 7;
        //StringOffsets
        int widthStringOffset = xmlParser.stringChunkList.size() * 4;
        //String池中在该String之前的String占据字节数
        int widthStringPoolBeforeAttr = 0;
        for (String s : xmlParser.stringChunkList) {
            if (s.equals(newAttr)) {
                break;
            }
            widthStringPoolBeforeAttr += s.length() * 2 + 4;
        }
        int widthBeforeAttr = widthHead + widthStringTrunkHead + widthStringOffset + widthStringPoolBeforeAttr;
        //新字符串与原来的字符串字节数之差
        int diffByteCount = (newAttr.length() - oldAttr.length()) * 2;
        if (diffByteCount == 0) {
            //修改String Pool中的字符串, 还有两个字节用来表示字符串的长度
            ByteUtils.replaceBytes(xmlParser.reader.data,
                    widthBeforeAttr + 2,
                    ByteUtils.string2Byte(newAttr));
        } else {
            //修改String Pool中的字符串, 还有两个字节用来表示字符串的长度
            xmlParser.reader.data = ByteUtils.replaceBytes(
                    xmlParser.reader.data,
                    widthBeforeAttr + 2,
                    oldAttr.length() * 2,
                    ByteUtils.string2Byte(newAttr));
            //修改String Pool中的字符串长度
            ByteUtils.replaceBytes(xmlParser.reader.data,
                    widthBeforeAttr,
                    ByteUtils.short2Byte((short) newAttr.length()));
            //修改String Offsets
            for (int i = attrValueIndex + 1; i < xmlParser.stringChunkList.size(); i++) {
                ByteUtils.replaceBytes(
                        xmlParser.reader.data,
                        widthHead + widthStringTrunkHead + 4 * i,
                        ByteUtils.int2Byte(ByteUtils.byte2int(
                                Arrays.copyOfRange(
                                        xmlParser.reader.data,
                                        widthHead + widthStringTrunkHead + 4 * i,
                                        widthHead + widthStringTrunkHead + 4 * i + 4
                                )
                        ) + diffByteCount)
                );
            }
            //判断【String Pool内的字符串字节数】（不是String Trunk的字节数）是否是4的倍数
            int stringPoolSize =
                    (xmlParser.stylePoolOffset == 0 ? xmlParser.stringTrunkSize : xmlParser.stylePoolOffset)
                    - xmlParser.stringPoolOffset + diffByteCount;
            if (stringPoolSize % 4 != 0) {
                diffByteCount += 2;
                //在String Pool中的字符串最后添加两个字节补齐
                xmlParser.reader.data = ByteUtils.replaceBytes(
                        xmlParser.reader.data,
                        widthHead + xmlParser.stringPoolOffset + stringPoolSize,
                        0,
                        new byte[]{0, 0}
                );
            }
            //修改File Size
            ByteUtils.replaceBytes(
                    xmlParser.reader.data,
                    4,
                    ByteUtils.int2Byte(xmlParser.fileSize + diffByteCount)
            );
            //修改String Chunk的大小
            ByteUtils.replaceBytes(
                    xmlParser.reader.data,
                    widthHead + 4,
                    ByteUtils.int2Byte(xmlParser.stringTrunkSize + diffByteCount)
            );
        }
        return this;
    }
}
