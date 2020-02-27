package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class AuthorityKeyIdentifierExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.AuthorityKeyIdentifier";
   public static final String NAME = "AuthorityKeyIdentifier";
   public static final String KEY_ID = "key_id";
   public static final String AUTH_NAME = "auth_name";
   public static final String SERIAL_NUMBER = "serial_number";
   private static final byte TAG_ID = 0;
   private static final byte TAG_NAMES = 1;
   private static final byte TAG_SERIAL_NUM = 2;
   private KeyIdentifier id = null;
   private GeneralNames names = null;
   private SerialNumber serialNum = null;

   private void encodeThis() throws IOException {
      if (this.id == null && this.names == null && this.serialNum == null) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();
         DerOutputStream var3;
         if (this.id != null) {
            var3 = new DerOutputStream();
            this.id.encode(var3);
            var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), var3);
         }

         try {
            if (this.names != null) {
               var3 = new DerOutputStream();
               this.names.encode(var3);
               var2.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), var3);
            }
         } catch (Exception var4) {
            throw new IOException(var4.toString());
         }

         if (this.serialNum != null) {
            var3 = new DerOutputStream();
            this.serialNum.encode(var3);
            var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)2), var3);
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.extensionValue = var1.toByteArray();
      }
   }

   public AuthorityKeyIdentifierExtension(KeyIdentifier var1, GeneralNames var2, SerialNumber var3) throws IOException {
      this.id = var1;
      this.names = var2;
      this.serialNum = var3;
      this.extensionId = PKIXExtensions.AuthorityKey_Id;
      this.critical = false;
      this.encodeThis();
   }

   public AuthorityKeyIdentifierExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.AuthorityKey_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      if (var3.tag != 48) {
         throw new IOException("Invalid encoding for AuthorityKeyIdentifierExtension.");
      } else {
         while(true) {
            if (var3.data != null && var3.data.available() != 0) {
               DerValue var4 = var3.data.getDerValue();
               if (var4.isContextSpecific((byte)0) && !var4.isConstructed()) {
                  if (this.id != null) {
                     throw new IOException("Duplicate KeyIdentifier in AuthorityKeyIdentifier.");
                  }

                  var4.resetTag((byte)4);
                  this.id = new KeyIdentifier(var4);
                  continue;
               }

               if (var4.isContextSpecific((byte)1) && var4.isConstructed()) {
                  if (this.names != null) {
                     throw new IOException("Duplicate GeneralNames in AuthorityKeyIdentifier.");
                  }

                  var4.resetTag((byte)48);
                  this.names = new GeneralNames(var4);
                  continue;
               }

               if (var4.isContextSpecific((byte)2) && !var4.isConstructed()) {
                  if (this.serialNum != null) {
                     throw new IOException("Duplicate SerialNumber in AuthorityKeyIdentifier.");
                  }

                  var4.resetTag((byte)2);
                  this.serialNum = new SerialNumber(var4);
                  continue;
               }

               throw new IOException("Invalid encoding of AuthorityKeyIdentifierExtension.");
            }

            return;
         }
      }
   }

   public String toString() {
      String var1 = super.toString() + "AuthorityKeyIdentifier [\n";
      if (this.id != null) {
         var1 = var1 + this.id.toString();
      }

      if (this.names != null) {
         var1 = var1 + this.names.toString() + "\n";
      }

      if (this.serialNum != null) {
         var1 = var1 + this.serialNum.toString() + "\n";
      }

      return var1 + "]\n";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.AuthorityKey_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         if (!(var2 instanceof KeyIdentifier)) {
            throw new IOException("Attribute value should be of type KeyIdentifier.");
         }

         this.id = (KeyIdentifier)var2;
      } else if (var1.equalsIgnoreCase("auth_name")) {
         if (!(var2 instanceof GeneralNames)) {
            throw new IOException("Attribute value should be of type GeneralNames.");
         }

         this.names = (GeneralNames)var2;
      } else {
         if (!var1.equalsIgnoreCase("serial_number")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
         }

         if (!(var2 instanceof SerialNumber)) {
            throw new IOException("Attribute value should be of type SerialNumber.");
         }

         this.serialNum = (SerialNumber)var2;
      }

      this.encodeThis();
   }

   public Object get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         return this.id;
      } else if (var1.equalsIgnoreCase("auth_name")) {
         return this.names;
      } else if (var1.equalsIgnoreCase("serial_number")) {
         return this.serialNum;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         this.id = null;
      } else if (var1.equalsIgnoreCase("auth_name")) {
         this.names = null;
      } else {
         if (!var1.equalsIgnoreCase("serial_number")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
         }

         this.serialNum = null;
      }

      this.encodeThis();
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("key_id");
      var1.addElement("auth_name");
      var1.addElement("serial_number");
      return var1.elements();
   }

   public String getName() {
      return "AuthorityKeyIdentifier";
   }

   public byte[] getEncodedKeyIdentifier() throws IOException {
      if (this.id != null) {
         DerOutputStream var1 = new DerOutputStream();
         this.id.encode(var1);
         return var1.toByteArray();
      } else {
         return null;
      }
   }
}
