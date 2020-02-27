package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class AccessDescription {
   private int myhash = -1;
   private ObjectIdentifier accessMethod;
   private GeneralName accessLocation;
   public static final ObjectIdentifier Ad_OCSP_Id = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1});
   public static final ObjectIdentifier Ad_CAISSUERS_Id = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 2});
   public static final ObjectIdentifier Ad_TIMESTAMPING_Id = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 3});
   public static final ObjectIdentifier Ad_CAREPOSITORY_Id = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 5});

   public AccessDescription(ObjectIdentifier var1, GeneralName var2) {
      this.accessMethod = var1;
      this.accessLocation = var2;
   }

   public AccessDescription(DerValue var1) throws IOException {
      DerInputStream var2 = var1.getData();
      this.accessMethod = var2.getOID();
      this.accessLocation = new GeneralName(var2.getDerValue());
   }

   public ObjectIdentifier getAccessMethod() {
      return this.accessMethod;
   }

   public GeneralName getAccessLocation() {
      return this.accessLocation;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      var2.putOID(this.accessMethod);
      this.accessLocation.encode(var2);
      var1.write((byte)48, (DerOutputStream)var2);
   }

   public int hashCode() {
      if (this.myhash == -1) {
         this.myhash = this.accessMethod.hashCode() + this.accessLocation.hashCode();
      }

      return this.myhash;
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof AccessDescription) {
         AccessDescription var2 = (AccessDescription)var1;
         if (this == var2) {
            return true;
         } else {
            return this.accessMethod.equals((Object)var2.getAccessMethod()) && this.accessLocation.equals(var2.getAccessLocation());
         }
      } else {
         return false;
      }
   }

   public String toString() {
      String var1 = null;
      if (this.accessMethod.equals((Object)Ad_CAISSUERS_Id)) {
         var1 = "caIssuers";
      } else if (this.accessMethod.equals((Object)Ad_CAREPOSITORY_Id)) {
         var1 = "caRepository";
      } else if (this.accessMethod.equals((Object)Ad_TIMESTAMPING_Id)) {
         var1 = "timeStamping";
      } else if (this.accessMethod.equals((Object)Ad_OCSP_Id)) {
         var1 = "ocsp";
      } else {
         var1 = this.accessMethod.toString();
      }

      return "\n   accessMethod: " + var1 + "\n   accessLocation: " + this.accessLocation.toString() + "\n";
   }
}
