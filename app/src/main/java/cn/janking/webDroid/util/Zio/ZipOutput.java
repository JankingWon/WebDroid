package cn.janking.webDroid.util.Zio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ZipOutput {
    List<ZioEntry> entriesWritten = new LinkedList<ZioEntry>();

    int filePointer = 0;

    Set<String> namesWritten = new HashSet<String>();

    OutputStream out = (OutputStream)null;

    String outputFilename;

    public ZipOutput(File paramFile) throws IOException {
        this.outputFilename = paramFile.getAbsolutePath();
        init(paramFile);
    }

    public ZipOutput(OutputStream paramOutputStream) throws IOException { this.out = paramOutputStream; }

    public ZipOutput(String paramString) throws IOException {
        this.outputFilename = paramString;
        init(new File(this.outputFilename));
    }

    private void init(File paramFile) throws IOException {
        if (paramFile.exists())
            paramFile.delete();
        this.out = new FileOutputStream(paramFile);
    }

    public void close() throws IOException { // Byte code:
        //   0: new apksigner/io/CentralEnd
        //   3: dup
        //   4: invokespecial <init> : ()V
        //   7: astore_2
        //   8: aload_2
        //   9: aload_0
        //   10: invokevirtual getFilePointer : ()I
        //   13: putfield centralStartOffset : I
        //   16: aload_0
        //   17: getfield entriesWritten : Ljava/util/List;
        //   20: invokeinterface size : ()I
        //   25: i2s
        //   26: istore_1
        //   27: aload_2
        //   28: iload_1
        //   29: putfield totalCentralEntries : S
        //   32: aload_2
        //   33: iload_1
        //   34: putfield numCentralEntries : S
        //   37: aload_0
        //   38: getfield entriesWritten : Ljava/util/List;
        //   41: checkcast java/util/Collection
        //   44: invokeinterface iterator : ()Ljava/util/Iterator;
        //   49: astore_3
        //   50: aload_3
        //   51: invokeinterface hasNext : ()Z
        //   56: ifne -> 98
        //   59: aload_2
        //   60: aload_0
        //   61: invokevirtual getFilePointer : ()I
        //   64: aload_2
        //   65: getfield centralStartOffset : I
        //   68: isub
        //   69: putfield centralDirectorySize : I
        //   72: aload_2
        //   73: ldc ''
        //   75: putfield fileComment : Ljava/lang/String;
        //   78: aload_2
        //   79: aload_0
        //   80: invokevirtual write : (Lapksigner/io/ZipOutput;)V
        //   83: aload_0
        //   84: getfield out : Ljava/io/OutputStream;
        //   87: ifnull -> 97
        //   90: aload_0
        //   91: getfield out : Ljava/io/OutputStream;
        //   94: invokevirtual close : ()V
        //   97: return
        //   98: aload_3
        //   99: invokeinterface next : ()Ljava/lang/Object;
        //   104: checkcast apksigner/io/ZioEntry
        //   107: aload_0
        //   108: invokevirtual write : (Lapksigner/io/ZipOutput;)V
        //   111: goto -> 50
        //   114: astore_2
        //   115: return
        // Exception table:
        //   from	to	target	type
        //   90	97	114	java/lang/Throwable
        }

        public int getFilePointer() throws IOException { return this.filePointer; }

        public void write(ZioEntry paramZioEntry) throws IOException {
            String str = paramZioEntry.getName();
            if (this.namesWritten.contains(str))
                return;
            paramZioEntry.writeLocalEntry(this);
            this.entriesWritten.add(paramZioEntry);
            this.namesWritten.add(str);
        }

        public void writeBytes(byte[] paramArrayOfbyte) throws IOException {
            this.out.write(paramArrayOfbyte);
            this.filePointer += paramArrayOfbyte.length;
        }

        public void writeBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException {
            this.out.write(paramArrayOfbyte, paramInt1, paramInt2);
            this.filePointer += paramInt2;
        }

        public void writeInt(int paramInt) throws IOException {
            byte[] arrayOfByte = new byte[4];
            int i = paramInt;
            for (paramInt = 0;; paramInt++) {
                if (paramInt >= 4) {
                    this.out.write(arrayOfByte);
                    this.filePointer += 4;
                    return;
                }
                arrayOfByte[paramInt] = (byte)(i & 0xFF);
                i >>= 8;
            }
        }

        public void writeShort(short paramShort) throws IOException {
            byte[] arrayOfByte = new byte[2];
            int i = paramShort;
            for (paramShort = 0;; paramShort++) {
                if (paramShort >= 2) {
                    this.out.write(arrayOfByte);
                    this.filePointer += 2;
                    return;
                }
                arrayOfByte[paramShort] = (byte)(i & 0xFF);
                i >>= 8;
            }
        }

        public void writeString(String paramString) throws IOException {
            byte[] arrayOfByte = paramString.getBytes();
            this.out.write(arrayOfByte);
            int i = this.filePointer;
            this.filePointer = arrayOfByte.length + i;
        }
    }
