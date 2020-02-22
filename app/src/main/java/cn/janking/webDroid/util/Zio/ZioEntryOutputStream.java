package cn.janking.webDroid.util.Zio;


import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class ZioEntryOutputStream extends OutputStream {
    CRC32 crc = new CRC32();

    int crcValue = 0;

    OutputStream downstream;

    int size = 0;

    OutputStream wrapped;

    public ZioEntryOutputStream(int paramInt, OutputStream paramOutputStream) {
        this.wrapped = paramOutputStream;
        if (paramInt != 0) {
            this.downstream = new DeflaterOutputStream(paramOutputStream, new Deflater(9, true));
            return;
        }
        this.downstream = paramOutputStream;
    }

    public void close() throws IOException {
        this.downstream.flush();
        this.downstream.close();
        this.crcValue = (int)this.crc.getValue();
    }

    public void flush() throws IOException { this.downstream.flush(); }

    public int getCRC() { return this.crcValue; }

    public int getSize() { return this.size; }

    public OutputStream getWrappedStream() { return this.wrapped; }

    public void write(int paramInt) throws IOException {
        this.downstream.write(paramInt);
        this.crc.update(paramInt);
        this.size++;
    }

    public void write(byte[] paramArrayOfbyte) throws IOException {
        this.downstream.write(paramArrayOfbyte);
        this.crc.update(paramArrayOfbyte);
        this.size += paramArrayOfbyte.length;
    }

    public void write(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException {
        this.downstream.write(paramArrayOfbyte, paramInt1, paramInt2);
        this.crc.update(paramArrayOfbyte, paramInt1, paramInt2);
        this.size += paramInt2;
    }
}
