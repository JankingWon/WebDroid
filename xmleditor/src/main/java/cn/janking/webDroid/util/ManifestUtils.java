package cn.janking.webDroid.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.wjdiankong.main.ParserChunkUtils;
import cn.wjdiankong.main.Utils;

public class ManifestUtils {
    private String filePath = null;

    public ManifestUtils(String srcFilePath, String destFilePath) throws IOException {
        if (destFilePath == null) {
            filePath = srcFilePath;
        } else {
            filePath = destFilePath;
        }
        FileInputStream fileInputStream = new FileInputStream(new File(srcFilePath));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        ParserChunkUtils.xmlStruct.byteSrc = byteArrayOutputStream.toByteArray();
        fileInputStream.close();
        byteArrayOutputStream.close();
    }

    public void exec() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
        fileOutputStream.write(ParserChunkUtils.xmlStruct.byteSrc);
        fileOutputStream.close();
    }

    public ManifestUtils check() throws UnsupportedEncodingException {
        ParserChunkUtils.parserXml();
        return this;
    }

    public ManifestUtils modifyStringAttribute(String oldAttr, String newAttr) throws IOException {
        ParserChunkUtils.parserXml();
        int attrValueIndex = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.indexOf(oldAttr);
        if(attrValueIndex == -1){
            throw new RuntimeException("找不到该属性值");
        }
        ParserChunkUtils.xmlStruct.stringChunk.stringContentList.set(attrValueIndex, newAttr);
        //如果字符串长度一样，只需要修改StringPool中的原来字符串即可
        //magicNumber和fileSize
        int widthHead = 4 * 2;
        //StringChunk中 ChunkType 到StylePoolOffset
        int widthStringTrunkHead = 4 * 7;
        //StringOffsets
        int widthStringOffset = ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size() * 4;
        //String池中在该String之前的String占据字节数
        int widthStringPool = 0;
        for (String s : ParserChunkUtils.xmlStruct.stringChunk.stringContentList) {
            if (s.equals(newAttr)) {
                break;
            }
            widthStringPool += s.length() * 2 + 4;
        }
        int totalWidth = widthHead + widthStringTrunkHead + widthStringOffset + widthStringPool;
        //新字符串与原来的字符串字节数之差
        int diffByteCount = (newAttr.length() - oldAttr.length()) * 2;
        if (diffByteCount == 0) {
            //修改String Pool中的字符串, 还有两个字节用来表示字符串的长度
            ByteUtils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, totalWidth + 2, ByteUtils.string2Byte(newAttr));
        }else {
            //修改String Pool中的字符串, 还有两个字节用来表示字符串的长度
            ParserChunkUtils.xmlStruct.byteSrc = ByteUtils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, totalWidth + 2, oldAttr.length() * 2, ByteUtils.string2Byte(newAttr));
            //修改File Size
            ByteUtils.replaceBytes(
                    ParserChunkUtils.xmlStruct.byteSrc,
                    4,
                    ByteUtils.int2Byte(ByteUtils.byte2int(ParserChunkUtils.xmlStruct.fileSize) + diffByteCount)
            );
            //修改String Chunk的大小
            ByteUtils.replaceBytes(
                    ParserChunkUtils.xmlStruct.byteSrc,
                    widthHead + 4,
                    ByteUtils.int2Byte(ByteUtils.byte2int(ParserChunkUtils.xmlStruct.stringChunk.size) + diffByteCount)
            );
            //修改Style Pool Offset
            ByteUtils.replaceBytes(
                    ParserChunkUtils.xmlStruct.byteSrc,
                    widthHead + 6 * 4,
                    ByteUtils.int2Byte(ByteUtils.byte2int(ParserChunkUtils.xmlStruct.stringChunk.stylePoolOffset) + diffByteCount)
            );
            //修改String Offsets
            for (int i = attrValueIndex + 1; i < ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size(); i++) {
                ByteUtils.replaceBytes(
                        ParserChunkUtils.xmlStruct.byteSrc,
                        widthHead + widthStringTrunkHead + 4 * i,
                        ByteUtils.int2Byte(ByteUtils.byte2int(
                                Utils.copyByte(
                                        ParserChunkUtils.xmlStruct.byteSrc,
                                        widthHead + widthStringTrunkHead + 4 * i,
                                        4
                                )
                        ) + diffByteCount)
                );
            }
            //修改String Pool中的字符串长度
            ByteUtils.replaceBytes(ParserChunkUtils.xmlStruct.byteSrc, totalWidth, ByteUtils.short2Byte((short) newAttr.length()));
        }
        return this;
    }
}
