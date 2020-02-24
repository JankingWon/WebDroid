package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;
import java.util.Enumeration;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateValidity implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.validity";
   public static final String NAME = "validity";
   public static final String NOT_BEFORE = "notBefore";
   public static final String NOT_AFTER = "notAfter";
   private static final long YR_2050 = 2524636800000L;
   private Date notBefore;
   private Date notAfter;

   private Date getNotBefore() {
      return new Date(this.notBefore.getTime());
   }

   private Date getNotAfter() {
      return new Date(this.notAfter.getTime());
   }

   private void construct(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("Invalid encoded CertificateValidity, starting sequence tag missing.");
      } else if (var1.data.available() == 0) {
         throw new IOException("No data encoded for CertificateValidity");
      } else {
         DerInputStream var2 = new DerInputStream(var1.toByteArray());
         DerValue[] var3 = var2.getSequence(2);
         if (var3.length != 2) {
            throw new IOException("Invalid encoding for CertificateValidity");
         } else {
            if (var3[0].tag == 23) {
               this.notBefore = var1.data.getUTCTime();
            } else {
               if (var3[0].tag != 24) {
                  throw new IOException("Invalid encoding for CertificateValidity");
               }

               this.notBefore = var1.data.getGeneralizedTime();
            }

            if (var3[1].tag == 23) {
               this.notAfter = var1.data.getUTCTime();
            } else {
               if (var3[1].tag != 24) {
                  throw new IOException("Invalid encoding for CertificateValidity");
               }

               this.notAfter = var1.data.getGeneralizedTime();
            }

         }
      }
   }

   public CertificateValidity() {
   }

   public CertificateValidity(Date var1, Date var2) {
      this.notBefore = var1;
      this.notAfter = var2;
   }

   public CertificateValidity(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      this.construct(var2);
   }

   public String toString() {
      return this.notBefore != null && this.notAfter != null ? "Validity: [From: " + this.notBefore.toString() + ",\n               To: " + this.notAfter.toString() + "]" : "";
   }

   public void encode(OutputStream var1) throws IOException {
      if (this.notBefore != null && this.notAfter != null) {
         DerOutputStream var2 = new DerOutputStream();
         if (this.notBefore.getTime() < 2524636800000L) {
            var2.putUTCTime(this.notBefore);
         } else {
            var2.putGeneralizedTime(this.notBefore);
         }

         if (this.notAfter.getTime() < 2524636800000L) {
            var2.putUTCTime(this.notAfter);
         } else {
            var2.putGeneralizedTime(this.notAfter);
         }

         DerOutputStream var3 = new DerOutputStream();
         var3.write((byte)48, (DerOutputStream)var2);
         var1.write(var3.toByteArray());
      } else {
         throw new IOException("CertAttrSet:CertificateValidity: null values to encode.\n");
      }
   }

   public void set(String var1, Object var2) throws IOException {
      if (!(var2 instanceof Date)) {
         throw new IOException("Attribute must be of type Date.");
      } else {
         if (var1.equalsIgnoreCase("notBefore")) {
            this.notBefore = (Date)var2;
         } else {
            if (!var1.equalsIgnoreCase("notAfter")) {
               throw new IOException("Attribute name not recognized by CertAttrSet: CertificateValidity.");
            }

            this.notAfter = (Date)var2;
         }

      }
   }

   public Date get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("notBefore")) {
         return this.getNotBefore();
      } else if (var1.equalsIgnoreCase("notAfter")) {
         return this.getNotAfter();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateValidity.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("notBefore")) {
         this.notBefore = null;
      } else {
         if (!var1.equalsIgnoreCase("notAfter")) {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateValidity.");
         }

         this.notAfter = null;
      }

   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("notBefore");
      var1.addElement("notAfter");
      return var1.elements();
   }

   public String getName() {
      return "validity";
   }

   public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
      Date var1 = new Date();
      this.valid(var1);
   }

   public void valid(Date var1) throws CertificateNotYetValidException, CertificateExpiredException {
      if (this.notBefore.after(var1)) {
         throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
      } else if (this.notAfter.before(var1)) {
         throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
      }
   }
}
