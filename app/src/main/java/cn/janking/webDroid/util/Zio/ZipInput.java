package cn.janking.webDroid.util.Zio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZipInput {
    CentralEnd centralEnd;

    long fileLength;

    RandomAccessFile in = (RandomAccessFile)null;

    public String inputFilename;

    Manifest manifest;

    int scanIterations = 0;

    Map<String, ZioEntry> zioEntries = new LinkedHashMap<String, ZioEntry>();

    public ZipInput(File paramFile) throws IOException {
        this.inputFilename = paramFile.getName();
        this.in = new RandomAccessFile(paramFile, "r");
        this.fileLength = this.in.length();
    }

    public ZipInput(String paramString) throws IOException {
        this.inputFilename = paramString;
        this.in = new RandomAccessFile(new File(this.inputFilename), "r");
        this.fileLength = this.in.length();
    }

    private void doRead() {
        try {
            long l = scanForEOCDR(256);
            this.in.seek(l);
            this.centralEnd = CentralEnd.read(this);
            this.in.seek(this.centralEnd.centralStartOffset);
            for (int i = 0;; i++) {
                if (i >= this.centralEnd.totalCentralEntries)
                    return;
                ZioEntry zioEntry = ZioEntry.read(this);
                this.zioEntries.put(zioEntry.getName(), zioEntry);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return;
        }
    }

    public static ZipInput read(File paramFile) throws IOException {
        ZipInput zipInput = new ZipInput(paramFile);
        zipInput.doRead();
        return zipInput;
    }

    public static ZipInput read(String paramString) throws IOException {
        ZipInput zipInput = new ZipInput(paramString);
        zipInput.doRead();
        return zipInput;
    }

    public void close() {
        if (this.in != null)
            try {
                this.in.close();
                return;
            } catch (Throwable throwable) {
                return;
            }
    }

    public Map<String, ZioEntry> getEntries() { return this.zioEntries; }

    public ZioEntry getEntry(String paramString) { return this.zioEntries.get(paramString); }

    public long getFileLength() { return this.fileLength; }

    public long getFilePointer() throws IOException { return this.in.getFilePointer(); }

    public String getFilename() { return this.inputFilename; }

    public Manifest getManifest() throws IOException {
        if (this.manifest == null) {
            ZioEntry zioEntry = this.zioEntries.get("META-INF/MANIFEST.MF");
            if (zioEntry != null)
                this.manifest = new Manifest(zioEntry.getInputStream());
        }
        return this.manifest;
    }

    public Collection<String> list(String paramString) {
        if (!paramString.endsWith("/"))
            throw new IllegalArgumentException("Invalid path -- does not end with '/'");
        String str = paramString;
        if (paramString.startsWith("/"))
            str = paramString.substring(1);
        Pattern pattern = Pattern.compile(String.format("^%s([^/]+/?).*", new Object[] { str }));
        TreeSet<String> treeSet = new TreeSet();
        Iterator<String> iterator = this.zioEntries.keySet().iterator();
        while (true) {
            if (!iterator.hasNext())
                return treeSet;
            Matcher matcher = pattern.matcher(iterator.next());
            if (matcher.matches())
                treeSet.add(matcher.group(1));
        }
    }

    public int read(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException { return this.in.read(paramArrayOfbyte, paramInt1, paramInt2); }

    public byte readByte() throws IOException { return this.in.readByte(); }

    public byte[] readBytes(int paramInt) throws IOException {
        byte[] arrayOfByte = new byte[paramInt];
        for (int i = 0;; i++) {
            if (i >= paramInt)
                return arrayOfByte;
            arrayOfByte[i] = this.in.readByte();
        }
    }

    public int readInt() throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            if (i >= 4)
                return j;
            j |= this.in.readUnsignedByte() << i * 8;
            i++;
        }
    }

    public short readShort() throws IOException {
        int i = 0;
        short s = (short)0;
        while (true) {
            if (i >= 2)
                return s;
            s = (short)(s | this.in.readUnsignedByte() << i * 8);
            i++;
        }
    }

    public String readString(int paramInt) throws IOException {
        byte[] arrayOfByte = new byte[paramInt];
        for (int i = 0;; i++) {
            if (i >= paramInt)
                return new String(arrayOfByte);
            arrayOfByte[i] = this.in.readByte();
        }
    }

    public long scanForEOCDR(int paramInt) throws IOException {
        if (paramInt > this.fileLength || paramInt > 65536)
            throw new IllegalStateException("End of central directory not found in " + this.inputFilename);
        int j = (int)Math.min(this.fileLength, paramInt);
        byte[] arrayOfByte = new byte[j];
        this.in.seek(this.fileLength - j);
        this.in.readFully(arrayOfByte);
        for (int i = j - 22;; i--) {
            if (i < 0)
                return scanForEOCDR(paramInt * 2);
            this.scanIterations++;
            if (arrayOfByte[i] == 80 && arrayOfByte[i + 1] == 75 && arrayOfByte[i + 2] == 5 && arrayOfByte[i + 3] == 6)
                return this.fileLength - j + i;
        }
    }

    public void seek(long paramLong) throws IOException { this.in.seek(paramLong); }
}
