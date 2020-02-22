package cn.janking.webDroid.util.Zio;

import java.io.IOException;

public class CentralEnd {
    public int centralDirectorySize;

    public short centralStartDisk = (short)0;

    public int centralStartOffset;

    public String fileComment;

    public short numCentralEntries;

    public short numberThisDisk = (short)0;

    public int signature = 101010256;

    public short totalCentralEntries;

    private void doRead(ZipInput paramZipInput) throws IOException {
        this.numberThisDisk = paramZipInput.readShort();
        this.centralStartDisk = paramZipInput.readShort();
        this.numCentralEntries = paramZipInput.readShort();
        this.totalCentralEntries = paramZipInput.readShort();
        this.centralDirectorySize = paramZipInput.readInt();
        this.centralStartOffset = paramZipInput.readInt();
        this.fileComment = paramZipInput.readString(paramZipInput.readShort());
    }

    public static CentralEnd read(ZipInput paramZipInput) throws IOException {
        if (paramZipInput.readInt() != 101010256) {
            paramZipInput.seek(paramZipInput.getFilePointer() - 4L);
            return (CentralEnd)null;
        }
        CentralEnd centralEnd = new CentralEnd();
        centralEnd.doRead(paramZipInput);
        return centralEnd;
    }

    public void write(ZipOutput paramZipOutput) throws IOException {
        paramZipOutput.writeInt(this.signature);
        paramZipOutput.writeShort(this.numberThisDisk);
        paramZipOutput.writeShort(this.centralStartDisk);
        paramZipOutput.writeShort(this.numCentralEntries);
        paramZipOutput.writeShort(this.totalCentralEntries);
        paramZipOutput.writeInt(this.centralDirectorySize);
        paramZipOutput.writeInt(this.centralStartOffset);
        paramZipOutput.writeShort((short)this.fileComment.length());
        paramZipOutput.writeString(this.fileComment);
    }
}
