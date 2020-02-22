package cn.janking.webDroid.util.Zio;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ZioEntry implements Cloneable {
    private static byte[] alignBytes = new byte[4];

    private int compressedSize;

    private short compression;

    private int crc32;

    private byte[] data = (byte[])null;

    private long dataPosition = -1L;

    private short diskNumberStart;

    private ZioEntryOutputStream entryOut = (ZioEntryOutputStream)null;

    private int externalAttributes;

    private byte[] extraData;

    private String fileComment;

    private String filename;

    private short generalPurposeBits;

    private short internalAttributes;

    private int localHeaderOffset;

    private short modificationDate;

    private short modificationTime;

    private short numAlignBytes = (short)0;

    private int size;

    private short versionMadeBy;

    private short versionRequired;

    private ZipInput zipInput;

    public ZioEntry(ZipInput paramZipInput) { this.zipInput = paramZipInput; }

    public ZioEntry(String paramString) {
        this.filename = paramString;
        this.fileComment = "";
        this.compression = (short)8;
        this.extraData = new byte[0];
        setTime(System.currentTimeMillis());
    }

    public ZioEntry(String paramString1, String paramString2) throws IOException {
        this.zipInput = new ZipInput(paramString2);
        this.filename = paramString1;
        this.fileComment = "";
        this.compression = (short)0;
        this.size = (int)this.zipInput.getFileLength();
        this.compressedSize = this.size;
        CRC32 cRC32 = new CRC32();
        byte[] arrayOfByte = new byte[8096];
        int i = 0;
        while (true) {
            if (i == this.size) {
                this.crc32 = (int)cRC32.getValue();
                this.zipInput.seek(0L);
                this.dataPosition = 0L;
                this.extraData = new byte[0];
                setTime((new File(paramString2)).lastModified());
                return;
            }
            int j = this.zipInput.read(arrayOfByte, 0, Math.min(arrayOfByte.length, this.size - i));
            if (j > 0) {
                cRC32.update(arrayOfByte, 0, j);
                i += j;
            }
        }
    }

    public ZioEntry(String paramString1, String paramString2, short paramShort, int paramInt1, int paramInt2, int paramInt3) throws IOException {
        this.zipInput = new ZipInput(paramString2);
        this.filename = paramString1;
        this.fileComment = "";
        this.compression = paramShort;
        this.crc32 = paramInt1;
        this.compressedSize = paramInt2;
        this.size = paramInt3;
        this.dataPosition = 0L;
        this.extraData = new byte[0];
        setTime((new File(paramString2)).lastModified());
    }

    private void doRead(ZipInput paramZipInput) throws IOException {
        this.versionMadeBy = paramZipInput.readShort();
        this.versionRequired = paramZipInput.readShort();
        this.generalPurposeBits = paramZipInput.readShort();
        if ((this.generalPurposeBits & 0xF7F1) != 0)
            throw new IllegalStateException("Can't handle general purpose bits == " + String.format("0x%04x", new Object[] { new Short(this.generalPurposeBits) }));
        this.compression = paramZipInput.readShort();
        this.modificationTime = paramZipInput.readShort();
        this.modificationDate = paramZipInput.readShort();
        this.crc32 = paramZipInput.readInt();
        this.compressedSize = paramZipInput.readInt();
        this.size = paramZipInput.readInt();
        short s1 = paramZipInput.readShort();
        short s2 = paramZipInput.readShort();
        short s3 = paramZipInput.readShort();
        this.diskNumberStart = paramZipInput.readShort();
        this.internalAttributes = paramZipInput.readShort();
        this.externalAttributes = paramZipInput.readInt();
        this.localHeaderOffset = paramZipInput.readInt();
        this.filename = paramZipInput.readString(s1);
        this.extraData = paramZipInput.readBytes(s2);
        this.fileComment = paramZipInput.readString(s3);
        this.generalPurposeBits = (short)(this.generalPurposeBits & 0x800);
        if (this.size == 0) {
            this.compressedSize = 0;
            this.compression = (short)0;
            this.crc32 = 0;
        }
    }

    public static ZioEntry read(ZipInput paramZipInput) throws IOException {
        if (paramZipInput.readInt() != 33639248) {
            paramZipInput.seek(paramZipInput.getFilePointer() - 4L);
            return (ZioEntry)null;
        }
        ZioEntry zioEntry = new ZioEntry(paramZipInput);
        zioEntry.doRead(paramZipInput);
        return zioEntry;
    }

    public ZioEntry getClonedEntry(String paramString) {
        try {
            ZioEntry zioEntry = (ZioEntry)clone();
            zioEntry.setName(paramString);
            return zioEntry;
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new IllegalStateException("clone() failed!");
        }
    }

    public int getCompressedSize() { return this.compressedSize; }

    public short getCompression() { return this.compression; }

    public int getCrc32() { return this.crc32; }

    public byte[] getData() throws IOException {
        if (this.data != null)
            return this.data;
        byte[] arrayOfByte = new byte[this.size];
        InputStream inputStream = getInputStream();
        for (int i = 0;; i += 1) {
            if (i == this.size)
                return arrayOfByte;
            int j = inputStream.read(arrayOfByte, i, this.size - i);
            if (j < 0)
                throw new IllegalStateException(String.format("Read failed, expecting %d bytes, got %d instead", this.size, i));
        }
    }

    public long getDataPosition() { return this.dataPosition; }

    public short getDiskNumberStart() { return this.diskNumberStart; }

    public ZioEntryOutputStream getEntryOut() { return this.entryOut; }

    public int getExternalAttributes() { return this.externalAttributes; }

    public byte[] getExtraData() { return this.extraData; }

    public String getFileComment() { return this.fileComment; }

    public short getGeneralPurposeBits() { return this.generalPurposeBits; }

    public InputStream getInputStream() throws IOException { return getInputStream((OutputStream)null); }

    public InputStream getInputStream(OutputStream paramOutputStream) throws IOException {
        ByteArrayInputStream byteArrayInputStream;
        if (this.entryOut != null) {
            this.entryOut.close();
            this.size = this.entryOut.getSize();
            this.data = ((ByteArrayOutputStream)this.entryOut.getWrappedStream()).toByteArray();
            this.compressedSize = this.data.length;
            this.crc32 = this.entryOut.getCRC();
            this.entryOut = (ZioEntryOutputStream)null;
            byteArrayInputStream = new ByteArrayInputStream(this.data);
            return (InputStream)((this.compression == 0) ? byteArrayInputStream : new InflaterInputStream(new SequenceInputStream(byteArrayInputStream, new ByteArrayInputStream(new byte[1])), new Inflater(true)));
        }
        ZioEntryInputStream zioEntryInputStream = new ZioEntryInputStream(this);
        if (paramOutputStream != null)
            zioEntryInputStream.setMonitorStream(paramOutputStream);
        if (this.compression != 0) {
            zioEntryInputStream.setReturnDummyByte(true);
            return new InflaterInputStream(zioEntryInputStream, new Inflater(true));
        }
        return zioEntryInputStream;
    }

    public short getInternalAttributes() { return this.internalAttributes; }

    public int getLocalHeaderOffset() { return this.localHeaderOffset; }

    public String getName() { return this.filename; }

    public OutputStream getOutputStream() {
        this.entryOut = new ZioEntryOutputStream(this.compression, new ByteArrayOutputStream());
        return this.entryOut;
    }

    public int getSize() { return this.size; }

    public long getTime() { return (new Date((this.modificationDate >> 9 & 0x7F) + 80, (this.modificationDate >> 5 & 0xF) - 1, this.modificationDate & 0x1F, this.modificationTime >> 11 & 0x1F, this.modificationTime >> 5 & 0x3F, this.modificationTime << 1 & 0x3E)).getTime(); }

    public short getVersionMadeBy() { return this.versionMadeBy; }

    public short getVersionRequired() { return this.versionRequired; }

    public ZipInput getZipInput() { return this.zipInput; }

    public boolean isDirectory() { return this.filename.endsWith("/"); }

    public void readLocalHeader() throws IOException {
        ZipInput zipInput1 = this.zipInput;
        zipInput1.seek(this.localHeaderOffset);
        if (zipInput1.readInt() != 67324752)
            throw new IllegalStateException(String.format("Local header not found at pos=0x%08x, file=%s", new Object[] { new Long(zipInput1.getFilePointer()), this.filename }));
        zipInput1.readShort();
        zipInput1.readShort();
        zipInput1.readShort();
        zipInput1.readShort();
        zipInput1.readShort();
        zipInput1.readInt();
        zipInput1.readInt();
        zipInput1.readInt();
        short s1 = zipInput1.readShort();
        short s2 = zipInput1.readShort();
        zipInput1.readString(s1);
        zipInput1.readBytes(s2);
        this.dataPosition = zipInput1.getFilePointer();
    }

    public void setCompression(int paramInt) { this.compression = (short)paramInt; }

    public void setName(String paramString) { this.filename = paramString; }

    public void setTime(long paramLong) {
        Date date = new Date(paramLong);
        int i = date.getYear() + 1900;
        if (i < 1980) {
            paramLong = 2162688L;
        } else {
            int j = date.getMonth();
            int k = date.getDate();
            int m = date.getHours();
            int n = date.getMinutes();
            paramLong = (date.getSeconds() >> 1 | i - 1980 << 25 | j + 1 << 21 | k << 16 | m << 11 | n << 5);
        }
        this.modificationDate = (short)(int)(paramLong >> 16L);
        this.modificationTime = (short)(int)(paramLong & 65535L);
    }

    public void write(ZipOutput paramZipOutput) throws IOException {
        paramZipOutput.writeInt(33639248);
        paramZipOutput.writeShort(this.versionMadeBy);
        paramZipOutput.writeShort(this.versionRequired);
        paramZipOutput.writeShort(this.generalPurposeBits);
        paramZipOutput.writeShort(this.compression);
        paramZipOutput.writeShort(this.modificationTime);
        paramZipOutput.writeShort(this.modificationDate);
        paramZipOutput.writeInt(this.crc32);
        paramZipOutput.writeInt(this.compressedSize);
        paramZipOutput.writeInt(this.size);
        paramZipOutput.writeShort((short)this.filename.length());
        paramZipOutput.writeShort((short)(this.extraData.length + this.numAlignBytes));
        paramZipOutput.writeShort((short)this.fileComment.length());
        paramZipOutput.writeShort(this.diskNumberStart);
        paramZipOutput.writeShort(this.internalAttributes);
        paramZipOutput.writeInt(this.externalAttributes);
        paramZipOutput.writeInt(this.localHeaderOffset);
        paramZipOutput.writeString(this.filename);
        paramZipOutput.writeBytes(this.extraData);
        if (this.numAlignBytes > 0)
            paramZipOutput.writeBytes(alignBytes, 0, this.numAlignBytes);
        paramZipOutput.writeString(this.fileComment);
    }

    public void writeLocalEntry(ZipOutput paramZipOutput) throws IOException {
        if (this.data == null && this.dataPosition < 0L && this.zipInput != null)
            readLocalHeader();
        this.localHeaderOffset = paramZipOutput.getFilePointer();
        if (this.entryOut != null) {
            this.entryOut.close();
            this.size = this.entryOut.getSize();
            this.data = ((ByteArrayOutputStream)this.entryOut.getWrappedStream()).toByteArray();
            this.compressedSize = this.data.length;
            this.crc32 = this.entryOut.getCRC();
        }
        paramZipOutput.writeInt(67324752);
        paramZipOutput.writeShort(this.versionRequired);
        paramZipOutput.writeShort(this.generalPurposeBits);
        paramZipOutput.writeShort(this.compression);
        paramZipOutput.writeShort(this.modificationTime);
        paramZipOutput.writeShort(this.modificationDate);
        paramZipOutput.writeInt(this.crc32);
        paramZipOutput.writeInt(this.compressedSize);
        paramZipOutput.writeInt(this.size);
        paramZipOutput.writeShort((short)this.filename.length());
        this.numAlignBytes = (short)0;
        if (this.compression == 0 && this.size > 0) {
            short s = (short)(int)((paramZipOutput.getFilePointer() + 2 + this.filename.length() + this.extraData.length) % 4L);
            if (s > 0)
                this.numAlignBytes = (short)(4 - s);
        }
        paramZipOutput.writeShort((short)(this.extraData.length + this.numAlignBytes));
        paramZipOutput.writeString(this.filename);
        paramZipOutput.writeBytes(this.extraData);
        if (this.numAlignBytes > 0)
            paramZipOutput.writeBytes(alignBytes, 0, this.numAlignBytes);
        if (this.data != null) {
            paramZipOutput.writeBytes(this.data);
            return;
        }
        this.zipInput.seek(this.dataPosition);
        int i = Math.min(this.compressedSize, 8096);
        byte[] arrayOfByte = new byte[i];
        long l = 0L;
        while (true) {
            if (l != this.compressedSize) {
                int j = this.zipInput.in.read(arrayOfByte, 0, (int)Math.min(this.compressedSize - l, i));
                if (j > 0) {
                    paramZipOutput.writeBytes(arrayOfByte, 0, j);
                    l += j;
                    continue;
                }
                break;
            }
            return;
        }
        throw new IllegalStateException(String.format("EOF reached while copying %s with %d bytes left to go", new Object[] { this.filename, new Long(this.compressedSize - l) }));
    }
}

