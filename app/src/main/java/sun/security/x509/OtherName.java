package sun.security.x509;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class OtherName implements GeneralNameInterface {
   private String name;
   private ObjectIdentifier oid;
   private byte[] nameValue = null;
   private GeneralNameInterface gni = null;
   private static final byte TAG_VALUE = 0;
   private int myhash = -1;

   public OtherName(ObjectIdentifier var1, byte[] var2) throws IOException {
      if (var1 != null && var2 != null) {
         this.oid = var1;
         this.nameValue = var2;
         this.gni = this.getGNI(var1, var2);
         if (this.gni != null) {
            this.name = this.gni.toString();
         } else {
            this.name = "Unrecognized ObjectIdentifier: " + var1.toString();
         }

      } else {
         throw new NullPointerException("parameters may not be null");
      }
   }

   public OtherName(DerValue var1) throws IOException {
      DerInputStream var2 = var1.toDerInputStream();
      this.oid = var2.getOID();
      DerValue var3 = var2.getDerValue();
      this.nameValue = var3.toByteArray();
      this.gni = this.getGNI(this.oid, this.nameValue);
      if (this.gni != null) {
         this.name = this.gni.toString();
      } else {
         this.name = "Unrecognized ObjectIdentifier: " + this.oid.toString();
      }

   }

   public ObjectIdentifier getOID() {
      return this.oid;
   }

   public byte[] getNameValue() {
      return (byte[])this.nameValue.clone();
   }

   private GeneralNameInterface getGNI(ObjectIdentifier var1, byte[] var2) throws IOException {
      try {
         Class var3 = OIDMap.getClass(var1);
         if (var3 == null) {
            return null;
         } else {
            Class[] var4 = new Class[]{Object.class};
            Constructor var5 = var3.getConstructor(var4);
            Object[] var6 = new Object[]{var2};
            GeneralNameInterface var7 = (GeneralNameInterface)var5.newInstance(var6);
            return var7;
         }
      } catch (Exception var8) {
         throw new IOException("Instantiation error: " + var8, var8);
      }
   }

   public int getType() {
      return 0;
   }

   public void encode(DerOutputStream var1) throws IOException {
      if (this.gni != null) {
         this.gni.encode(var1);
      } else {
         DerOutputStream var2 = new DerOutputStream();
         var2.putOID(this.oid);
         var2.write(DerValue.createTag((byte)-128, true, (byte)0), this.nameValue);
         var1.write((byte)48, (DerOutputStream)var2);
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof OtherName)) {
         return false;
      } else {
         OtherName var2 = (OtherName)var1;
         if (!var2.oid.equals((Object)this.oid)) {
            return false;
         } else {
            GeneralNameInterface var3 = null;

            try {
               var3 = this.getGNI(var2.oid, var2.nameValue);
            } catch (IOException var7) {
               return false;
            }

            boolean var4;
            if (var3 != null) {
               try {
                  var4 = var3.constrains(this) == 0;
               } catch (UnsupportedOperationException var6) {
                  var4 = false;
               }
            } else {
               var4 = Arrays.equals(this.nameValue, var2.nameValue);
            }

            return var4;
         }
      }
   }

   public int hashCode() {
      if (this.myhash == -1) {
         this.myhash = 37 + this.oid.hashCode();

         for(int var1 = 0; var1 < this.nameValue.length; ++var1) {
            this.myhash = 37 * this.myhash + this.nameValue[var1];
         }
      }

      return this.myhash;
   }

   public String toString() {
      return "Other-Name: " + this.name;
   }

   public int constrains(GeneralNameInterface var1) {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else {
         if (var1.getType() == 0) {
            throw new UnsupportedOperationException("Narrowing, widening, and matching are not supported for OtherName.");
         }

         var2 = -1;
      }

      return var2;
   }

   public int subtreeDepth() {
      throw new UnsupportedOperationException("subtreeDepth() not supported for generic OtherName");
   }
}
