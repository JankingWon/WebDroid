package sun.security.pkcs;

import java.io.IOException;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ContentInfo {
   private static int[] pkcs7 = new int[]{1, 2, 840, 113549, 1, 7};
   private static int[] data = new int[]{1, 2, 840, 113549, 1, 7, 1};
   private static int[] sdata = new int[]{1, 2, 840, 113549, 1, 7, 2};
   private static int[] edata = new int[]{1, 2, 840, 113549, 1, 7, 3};
   private static int[] sedata = new int[]{1, 2, 840, 113549, 1, 7, 4};
   private static int[] ddata = new int[]{1, 2, 840, 113549, 1, 7, 5};
   private static int[] crdata = new int[]{1, 2, 840, 113549, 1, 7, 6};
   private static int[] nsdata = new int[]{2, 16, 840, 1, 113730, 2, 5};
   private static int[] tstInfo = new int[]{1, 2, 840, 113549, 1, 9, 16, 1, 4};
   private static final int[] OLD_SDATA = new int[]{1, 2, 840, 1113549, 1, 7, 2};
   private static final int[] OLD_DATA = new int[]{1, 2, 840, 1113549, 1, 7, 1};
   public static ObjectIdentifier PKCS7_OID;
   public static ObjectIdentifier DATA_OID;
   public static ObjectIdentifier SIGNED_DATA_OID;
   public static ObjectIdentifier ENVELOPED_DATA_OID;
   public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID;
   public static ObjectIdentifier DIGESTED_DATA_OID;
   public static ObjectIdentifier ENCRYPTED_DATA_OID;
   public static ObjectIdentifier OLD_SIGNED_DATA_OID;
   public static ObjectIdentifier OLD_DATA_OID;
   public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID;
   public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID;
   ObjectIdentifier contentType;
   DerValue content;

   public ContentInfo(ObjectIdentifier var1, DerValue var2) {
      this.contentType = var1;
      this.content = var2;
   }

   public ContentInfo(byte[] var1) {
      DerValue var2 = new DerValue((byte)4, var1);
      this.contentType = DATA_OID;
      this.content = var2;
   }

   public ContentInfo(DerInputStream var1) throws IOException, ParsingException {
      this(var1, false);
   }

   public ContentInfo(DerInputStream var1, boolean var2) throws IOException, ParsingException {
      DerValue[] var7 = var1.getSequence(2);
      DerValue var5 = var7[0];
      DerInputStream var3 = new DerInputStream(var5.toByteArray());
      this.contentType = var3.getOID();
      if (var2) {
         this.content = var7[1];
      } else if (var7.length > 1) {
         DerValue var6 = var7[1];
         DerInputStream var4 = new DerInputStream(var6.toByteArray());
         DerValue[] var8 = var4.getSet(1, true);
         this.content = var8[0];
      }

   }

   public DerValue getContent() {
      return this.content;
   }

   public ObjectIdentifier getContentType() {
      return this.contentType;
   }

   public byte[] getData() throws IOException {
      if (!this.contentType.equals((Object)DATA_OID) && !this.contentType.equals((Object)OLD_DATA_OID) && !this.contentType.equals((Object)TIMESTAMP_TOKEN_INFO_OID)) {
         throw new IOException("content type is not DATA: " + this.contentType);
      } else {
         return this.content == null ? null : this.content.getOctetString();
      }
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var3 = new DerOutputStream();
      var3.putOID(this.contentType);
      if (this.content != null) {
         DerValue var4 = null;
         DerOutputStream var2 = new DerOutputStream();
         this.content.encode(var2);
         var4 = new DerValue((byte)-96, var2.toByteArray());
         var3.putDerValue(var4);
      }

      var1.write((byte)48, (DerOutputStream)var3);
   }

   public byte[] getContentBytes() throws IOException {
      if (this.content == null) {
         return null;
      } else {
         DerInputStream var1 = new DerInputStream(this.content.toByteArray());
         return var1.getOctetString();
      }
   }

   public String toString() {
      String var1 = "";
      var1 = var1 + "Content Info Sequence\n\tContent type: " + this.contentType + "\n";
      var1 = var1 + "\tContent: " + this.content;
      return var1;
   }

   static {
      PKCS7_OID = ObjectIdentifier.newInternal(pkcs7);
      DATA_OID = ObjectIdentifier.newInternal(data);
      SIGNED_DATA_OID = ObjectIdentifier.newInternal(sdata);
      ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(edata);
      SIGNED_AND_ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(sedata);
      DIGESTED_DATA_OID = ObjectIdentifier.newInternal(ddata);
      ENCRYPTED_DATA_OID = ObjectIdentifier.newInternal(crdata);
      OLD_SIGNED_DATA_OID = ObjectIdentifier.newInternal(OLD_SDATA);
      OLD_DATA_OID = ObjectIdentifier.newInternal(OLD_DATA);
      NETSCAPE_CERT_SEQUENCE_OID = ObjectIdentifier.newInternal(nsdata);
      TIMESTAMP_TOKEN_INFO_OID = ObjectIdentifier.newInternal(tstInfo);
   }
}
