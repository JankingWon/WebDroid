package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class OIDName implements GeneralNameInterface {
   private ObjectIdentifier oid;

   public OIDName(DerValue var1) throws IOException {
      this.oid = var1.getOID();
   }

   public OIDName(ObjectIdentifier var1) {
      this.oid = var1;
   }

   public OIDName(String var1) throws IOException {
      try {
         this.oid = new ObjectIdentifier(var1);
      } catch (Exception var3) {
         throw new IOException("Unable to create OIDName: " + var3);
      }
   }

   public int getType() {
      return 8;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putOID(this.oid);
   }

   public String toString() {
      return "OIDName: " + this.oid.toString();
   }

   public ObjectIdentifier getOID() {
      return this.oid;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof OIDName)) {
         return false;
      } else {
         OIDName var2 = (OIDName)var1;
         return this.oid.equals((Object)var2.oid);
      }
   }

   public int hashCode() {
      return this.oid.hashCode();
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else if (var1.getType() != 8) {
         var2 = -1;
      } else {
         if (!this.equals((OIDName)var1)) {
            throw new UnsupportedOperationException("Narrowing and widening are not supported for OIDNames");
         }

         var2 = 0;
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("subtreeDepth() not supported for OIDName.");
   }
}
