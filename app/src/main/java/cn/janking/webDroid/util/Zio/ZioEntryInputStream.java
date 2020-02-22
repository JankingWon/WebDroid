package cn.janking.webDroid.util.Zio;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class ZioEntryInputStream extends InputStream {
    OutputStream monitor = (OutputStream)null;

    int offset = 0;

    RandomAccessFile raf;

    boolean returnDummyByte = false;

    int size;

    public ZioEntryInputStream(ZioEntry paramZioEntry) throws IOException {
        this.size = paramZioEntry.getCompressedSize();
        this.raf = (paramZioEntry.getZipInput()).in;
        if (paramZioEntry.getDataPosition() >= 0L) {
            this.raf.seek(paramZioEntry.getDataPosition());
            return;
        }
        paramZioEntry.readLocalHeader();
    }

    private int readBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException {
        if (this.size - this.offset == 0) {
            if (this.returnDummyByte) {
                this.returnDummyByte = false;
                paramArrayOfbyte[paramInt1] = (byte)0;
                return 1;
            }
            return -1;
        }
        paramInt2 = Math.min(paramInt2, available());
        int i = this.raf.read(paramArrayOfbyte, paramInt1, paramInt2);
        paramInt2 = i;
        if (i > 0) {
            if (this.monitor != null)
                this.monitor.write(paramArrayOfbyte, paramInt1, i);
            this.offset += i;
            return i;
        }
        return paramInt2;
    }

    @Override
    public int available() throws IOException {
        int j = this.size - this.offset;
        int i = j;
        if (j == 0) {
            i = j;
            if (this.returnDummyByte)
                i = 1;
        }
        return i;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public boolean markSupported() { return false; }

    @Override
    public int read() throws IOException {
        int i = 0;
        if (this.size - this.offset == 0) {
            if (this.returnDummyByte) {
                this.returnDummyByte = false;
                return i;
            }
            return -1;
        }
        int j = this.raf.read();
        i = j;
        if (j >= 0) {
            if (this.monitor != null)
                this.monitor.write(j);
            this.offset++;
            return j;
        }
        return i;
    }

    @Override
    public int read(byte[] paramArrayOfbyte) throws IOException { return readBytes(paramArrayOfbyte, 0, paramArrayOfbyte.length); }

    @Override
    public int read(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException { return readBytes(paramArrayOfbyte, paramInt1, paramInt2); }

    public void setMonitorStream(OutputStream paramOutputStream) { this.monitor = paramOutputStream; }

    public void setReturnDummyByte(boolean paramBoolean) { this.returnDummyByte = paramBoolean; }

    @Override
    public long skip(long paramLong) throws IOException {
        paramLong = Math.min(paramLong, available());
        this.raf.seek(this.raf.getFilePointer() + paramLong);
        return paramLong;
    }
}
