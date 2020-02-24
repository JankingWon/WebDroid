package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class PKCS9Attributes {
   private final Hashtable<ObjectIdentifier, PKCS9Attribute> attributes;
   private final Hashtable<ObjectIdentifier, ObjectIdentifier> permittedAttributes;
   private final byte[] derEncoding;
   private boolean ignoreUnsupportedAttributes;

   public PKCS9Attributes(ObjectIdentifier[] var1, DerInputStream var2) throws IOException {
      this.attributes = new Hashtable(3);
      this.ignoreUnsupportedAttributes = false;
      if (var1 != null) {
         this.permittedAttributes = new Hashtable(var1.length);

         for(int var3 = 0; var3 < var1.length; ++var3) {
            this.permittedAttributes.put(var1[var3], var1[var3]);
         }
      } else {
         this.permittedAttributes = null;
      }

      this.derEncoding = this.decode(var2);
   }

   public PKCS9Attributes(DerInputStream var1) throws IOException {
      this(var1, false);
   }

   public PKCS9Attributes(DerInputStream var1, boolean var2) throws IOException {
      this.attributes = new Hashtable(3);
      this.ignoreUnsupportedAttributes = false;
      this.ignoreUnsupportedAttributes = var2;
      this.derEncoding = this.decode(var1);
      this.permittedAttributes = null;
   }

   public PKCS9Attributes(PKCS9Attribute[] var1) throws IllegalArgumentException, IOException {
      this.attributes = new Hashtable(3);
      this.ignoreUnsupportedAttributes = false;

      for(int var3 = 0; var3 < var1.length; ++var3) {
         ObjectIdentifier var2 = var1[var3].getOID();
         if (this.attributes.containsKey(var2)) {
            throw new IllegalArgumentException("PKCSAttribute " + var1[var3].getOID() + " duplicated while constructing PKCS9Attributes.");
         }

         this.attributes.put(var2, var1[var3]);
      }

      this.derEncoding = this.generateDerEncoding();
      this.permittedAttributes = null;
   }

   private byte[] decode(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      byte[] var3 = var2.toByteArray();
      var3[0] = 49;
      DerInputStream var4 = new DerInputStream(var3);
      DerValue[] var5 = var4.getSet(3, true);
      boolean var8 = true;

      for(int var9 = 0; var9 < var5.length; ++var9) {
         PKCS9Attribute var6;
         try {
            var6 = new PKCS9Attribute(var5[var9]);
         } catch (ParsingException var11) {
            if (this.ignoreUnsupportedAttributes) {
               var8 = false;
               continue;
            }

            throw var11;
         }

         ObjectIdentifier var7 = var6.getOID();
         if (this.attributes.get(var7) != null) {
            throw new IOException("Duplicate PKCS9 attribute: " + var7);
         }

         if (this.permittedAttributes != null && !this.permittedAttributes.containsKey(var7)) {
            throw new IOException("Attribute " + var7 + " not permitted in this attribute set");
         }

         this.attributes.put(var7, var6);
      }

      return var8 ? var3 : this.generateDerEncoding();
   }

   public void encode(byte var1, OutputStream var2) throws IOException {
      var2.write(var1);
      var2.write(this.derEncoding, 1, this.derEncoding.length - 1);
   }

   private byte[] generateDerEncoding() throws IOException {
      DerOutputStream var1 = new DerOutputStream();
      Object[] var2 = this.attributes.values().toArray();
      var1.putOrderedSetOf((byte)49, castToDerEncoder(var2));
      return var1.toByteArray();
   }

   public byte[] getDerEncoding() throws IOException {
      return (byte[])this.derEncoding.clone();
   }

   public PKCS9Attribute getAttribute(ObjectIdentifier var1) {
      return (PKCS9Attribute)this.attributes.get(var1);
   }

   public PKCS9Attribute getAttribute(String var1) {
      return (PKCS9Attribute)this.attributes.get(PKCS9Attribute.getOID(var1));
   }

   public PKCS9Attribute[] getAttributes() {
      PKCS9Attribute[] var1 = new PKCS9Attribute[this.attributes.size()];
      int var3 = 0;

      for(int var4 = 1; var4 < PKCS9Attribute.PKCS9_OIDS.length && var3 < var1.length; ++var4) {
         var1[var3] = this.getAttribute(PKCS9Attribute.PKCS9_OIDS[var4]);
         if (var1[var3] != null) {
            ++var3;
         }
      }

      return var1;
   }

   public Object getAttributeValue(ObjectIdentifier var1) throws IOException {
      try {
         Object var2 = this.getAttribute(var1).getValue();
         return var2;
      } catch (NullPointerException var3) {
         throw new IOException("No value found for attribute " + var1);
      }
   }

   public Object getAttributeValue(String var1) throws IOException {
      ObjectIdentifier var2 = PKCS9Attribute.getOID(var1);
      if (var2 == null) {
         throw new IOException("Attribute name " + var1 + " not recognized or not supported.");
      } else {
         return this.getAttributeValue(var2);
      }
   }

   public String toString() {
      StringBuffer var1 = new StringBuffer(200);
      var1.append("PKCS9 Attributes: [\n\t");
      boolean var4 = true;

      for(int var5 = 1; var5 < PKCS9Attribute.PKCS9_OIDS.length; ++var5) {
         PKCS9Attribute var3 = this.getAttribute(PKCS9Attribute.PKCS9_OIDS[var5]);
         if (var3 != null) {
            if (var4) {
               var4 = false;
            } else {
               var1.append(";\n\t");
            }

            var1.append(var3.toString());
         }
      }

      var1.append("\n\t] (end PKCS9 Attributes)");
      return var1.toString();
   }

   static DerEncoder[] castToDerEncoder(Object[] var0) {
      DerEncoder[] var1 = new DerEncoder[var0.length];

      for(int var2 = 0; var2 < var1.length; ++var2) {
         var1[var2] = (DerEncoder)var0[var2];
      }

      return var1;
   }
}
