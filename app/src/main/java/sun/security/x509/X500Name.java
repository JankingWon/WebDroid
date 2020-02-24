package sun.security.x509;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X500Name implements GeneralNameInterface, Principal {
   private String dn;
   private String rfc1779Dn;
   private String rfc2253Dn;
   private String canonicalDn;
   private RDN[] names;
   private X500Principal x500Principal;
   private byte[] encoded;
   private volatile List<RDN> rdnList;
   private volatile List<AVA> allAvaList;
   private static final int[] commonName_data = new int[]{2, 5, 4, 3};
   private static final int[] SURNAME_DATA = new int[]{2, 5, 4, 4};
   private static final int[] SERIALNUMBER_DATA = new int[]{2, 5, 4, 5};
   private static final int[] countryName_data = new int[]{2, 5, 4, 6};
   private static final int[] localityName_data = new int[]{2, 5, 4, 7};
   private static final int[] stateName_data = new int[]{2, 5, 4, 8};
   private static final int[] streetAddress_data = new int[]{2, 5, 4, 9};
   private static final int[] orgName_data = new int[]{2, 5, 4, 10};
   private static final int[] orgUnitName_data = new int[]{2, 5, 4, 11};
   private static final int[] title_data = new int[]{2, 5, 4, 12};
   private static final int[] GIVENNAME_DATA = new int[]{2, 5, 4, 42};
   private static final int[] INITIALS_DATA = new int[]{2, 5, 4, 43};
   private static final int[] GENERATIONQUALIFIER_DATA = new int[]{2, 5, 4, 44};
   private static final int[] DNQUALIFIER_DATA = new int[]{2, 5, 4, 46};
   private static final int[] ipAddress_data = new int[]{1, 3, 6, 1, 4, 1, 42, 2, 11, 2, 1};
   private static final int[] DOMAIN_COMPONENT_DATA = new int[]{0, 9, 2342, 19200300, 100, 1, 25};
   private static final int[] userid_data = new int[]{0, 9, 2342, 19200300, 100, 1, 1};
   public static final ObjectIdentifier commonName_oid;
   public static final ObjectIdentifier SERIALNUMBER_OID;
   public static final ObjectIdentifier countryName_oid;
   public static final ObjectIdentifier localityName_oid;
   public static final ObjectIdentifier orgName_oid;
   public static final ObjectIdentifier orgUnitName_oid;
   public static final ObjectIdentifier stateName_oid;
   public static final ObjectIdentifier streetAddress_oid;
   public static final ObjectIdentifier title_oid;
   public static final ObjectIdentifier DNQUALIFIER_OID;
   public static final ObjectIdentifier SURNAME_OID;
   public static final ObjectIdentifier GIVENNAME_OID;
   public static final ObjectIdentifier INITIALS_OID;
   public static final ObjectIdentifier GENERATIONQUALIFIER_OID;
   public static final ObjectIdentifier ipAddress_oid;
   public static final ObjectIdentifier DOMAIN_COMPONENT_OID;
   public static final ObjectIdentifier userid_oid;
   private static final Constructor<X500Principal> principalConstructor;
   private static final Field principalField;

   public X500Name(String var1) throws IOException {
      this(var1, Collections.emptyMap());
   }

   public X500Name(String var1, Map<String, String> var2) throws IOException {
      this.parseDN(var1, var2);
   }

   public X500Name(String var1, String var2) throws IOException {
      if (var1 == null) {
         throw new NullPointerException("Name must not be null");
      } else {
         if (var2.equalsIgnoreCase("RFC2253")) {
            this.parseRFC2253DN(var1);
         } else {
            if (!var2.equalsIgnoreCase("DEFAULT")) {
               throw new IOException("Unsupported format " + var2);
            }

            this.parseDN(var1, Collections.emptyMap());
         }

      }
   }

   public X500Name(String var1, String var2, String var3, String var4) throws IOException {
      this.names = new RDN[4];
      this.names[3] = new RDN(1);
      this.names[3].assertion[0] = new AVA(commonName_oid, new DerValue(var1));
      this.names[2] = new RDN(1);
      this.names[2].assertion[0] = new AVA(orgUnitName_oid, new DerValue(var2));
      this.names[1] = new RDN(1);
      this.names[1].assertion[0] = new AVA(orgName_oid, new DerValue(var3));
      this.names[0] = new RDN(1);
      this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(var4));
   }

   public X500Name(String var1, String var2, String var3, String var4, String var5, String var6) throws IOException {
      this.names = new RDN[6];
      this.names[5] = new RDN(1);
      this.names[5].assertion[0] = new AVA(commonName_oid, new DerValue(var1));
      this.names[4] = new RDN(1);
      this.names[4].assertion[0] = new AVA(orgUnitName_oid, new DerValue(var2));
      this.names[3] = new RDN(1);
      this.names[3].assertion[0] = new AVA(orgName_oid, new DerValue(var3));
      this.names[2] = new RDN(1);
      this.names[2].assertion[0] = new AVA(localityName_oid, new DerValue(var4));
      this.names[1] = new RDN(1);
      this.names[1].assertion[0] = new AVA(stateName_oid, new DerValue(var5));
      this.names[0] = new RDN(1);
      this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(var6));
   }

   public X500Name(RDN[] var1) throws IOException {
      if (var1 == null) {
         this.names = new RDN[0];
      } else {
         this.names = (RDN[])var1.clone();

         for(int var2 = 0; var2 < this.names.length; ++var2) {
            if (this.names[var2] == null) {
               throw new IOException("Cannot create an X500Name");
            }
         }
      }

   }

   public X500Name(DerValue var1) throws IOException {
      this(var1.toDerInputStream());
   }

   public X500Name(DerInputStream var1) throws IOException {
      this.parseDER(var1);
   }

   public X500Name(byte[] var1) throws IOException {
      DerInputStream var2 = new DerInputStream(var1);
      this.parseDER(var2);
   }

   public List<RDN> rdns() {
      List var1 = this.rdnList;
      if (var1 == null) {
         var1 = Collections.unmodifiableList(Arrays.asList(this.names));
         this.rdnList = var1;
      }

      return var1;
   }

   public int size() {
      return this.names.length;
   }

   public List<AVA> allAvas() {
      List var1 = this.allAvaList;
      if (var1 == null) {
         ArrayList var3 = new ArrayList();

         for(int var2 = 0; var2 < this.names.length; ++var2) {
            var3.addAll(this.names[var2].avas());
         }

         var1 = Collections.unmodifiableList(var3);
         this.allAvaList = var1;
      }

      return var1;
   }

   public int avaSize() {
      return this.allAvas().size();
   }

   public boolean isEmpty() {
      int var1 = this.names.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         if (this.names[var2].assertion.length != 0) {
            return false;
         }
      }

      return true;
   }

   public int hashCode() {
      return this.getRFC2253CanonicalName().hashCode();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof X500Name)) {
         return false;
      } else {
         X500Name var2 = (X500Name)var1;
         if (this.canonicalDn != null && var2.canonicalDn != null) {
            return this.canonicalDn.equals(var2.canonicalDn);
         } else {
            int var3 = this.names.length;
            if (var3 != var2.names.length) {
               return false;
            } else {
               for(int var4 = 0; var4 < var3; ++var4) {
                  RDN var5 = this.names[var4];
                  RDN var6 = var2.names[var4];
                  if (var5.assertion.length != var6.assertion.length) {
                     return false;
                  }
               }

               String var7 = this.getRFC2253CanonicalName();
               String var8 = var2.getRFC2253CanonicalName();
               return var7.equals(var8);
            }
         }
      }
   }

   private String getString(DerValue var1) throws IOException {
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.getAsString();
         if (var2 == null) {
            throw new IOException("not a DER string encoding, " + var1.tag);
         } else {
            return var2;
         }
      }
   }

   public int getType() {
      return 4;
   }

   public String getCountry() throws IOException {
      DerValue var1 = this.findAttribute(countryName_oid);
      return this.getString(var1);
   }

   public String getOrganization() throws IOException {
      DerValue var1 = this.findAttribute(orgName_oid);
      return this.getString(var1);
   }

   public String getOrganizationalUnit() throws IOException {
      DerValue var1 = this.findAttribute(orgUnitName_oid);
      return this.getString(var1);
   }

   public String getCommonName() throws IOException {
      DerValue var1 = this.findAttribute(commonName_oid);
      return this.getString(var1);
   }

   public String getLocality() throws IOException {
      DerValue var1 = this.findAttribute(localityName_oid);
      return this.getString(var1);
   }

   public String getState() throws IOException {
      DerValue var1 = this.findAttribute(stateName_oid);
      return this.getString(var1);
   }

   public String getDomain() throws IOException {
      DerValue var1 = this.findAttribute(DOMAIN_COMPONENT_OID);
      return this.getString(var1);
   }

   public String getDNQualifier() throws IOException {
      DerValue var1 = this.findAttribute(DNQUALIFIER_OID);
      return this.getString(var1);
   }

   public String getSurname() throws IOException {
      DerValue var1 = this.findAttribute(SURNAME_OID);
      return this.getString(var1);
   }

   public String getGivenName() throws IOException {
      DerValue var1 = this.findAttribute(GIVENNAME_OID);
      return this.getString(var1);
   }

   public String getInitials() throws IOException {
      DerValue var1 = this.findAttribute(INITIALS_OID);
      return this.getString(var1);
   }

   public String getGeneration() throws IOException {
      DerValue var1 = this.findAttribute(GENERATIONQUALIFIER_OID);
      return this.getString(var1);
   }

   public String getIP() throws IOException {
      DerValue var1 = this.findAttribute(ipAddress_oid);
      return this.getString(var1);
   }

   public String toString() {
      if (this.dn == null) {
         this.generateDN();
      }

      return this.dn;
   }

   public String getRFC1779Name() {
      return this.getRFC1779Name(Collections.emptyMap());
   }

   public String getRFC1779Name(Map<String, String> var1) throws IllegalArgumentException {
      if (var1.isEmpty()) {
         if (this.rfc1779Dn != null) {
            return this.rfc1779Dn;
         } else {
            this.rfc1779Dn = this.generateRFC1779DN(var1);
            return this.rfc1779Dn;
         }
      } else {
         return this.generateRFC1779DN(var1);
      }
   }

   public String getRFC2253Name() {
      return this.getRFC2253Name(Collections.emptyMap());
   }

   public String getRFC2253Name(Map<String, String> var1) {
      if (var1.isEmpty()) {
         if (this.rfc2253Dn != null) {
            return this.rfc2253Dn;
         } else {
            this.rfc2253Dn = this.generateRFC2253DN(var1);
            return this.rfc2253Dn;
         }
      } else {
         return this.generateRFC2253DN(var1);
      }
   }

   private String generateRFC2253DN(Map<String, String> var1) {
      if (this.names.length == 0) {
         return "";
      } else {
         StringBuilder var2 = new StringBuilder(48);

         for(int var3 = this.names.length - 1; var3 >= 0; --var3) {
            if (var3 < this.names.length - 1) {
               var2.append(',');
            }

            var2.append(this.names[var3].toRFC2253String(var1));
         }

         return var2.toString();
      }
   }

   public String getRFC2253CanonicalName() {
      if (this.canonicalDn != null) {
         return this.canonicalDn;
      } else if (this.names.length == 0) {
         this.canonicalDn = "";
         return this.canonicalDn;
      } else {
         StringBuilder var1 = new StringBuilder(48);

         for(int var2 = this.names.length - 1; var2 >= 0; --var2) {
            if (var2 < this.names.length - 1) {
               var1.append(',');
            }

            var1.append(this.names[var2].toRFC2253String(true));
         }

         this.canonicalDn = var1.toString();
         return this.canonicalDn;
      }
   }

   public String getName() {
      return this.toString();
   }

   private DerValue findAttribute(ObjectIdentifier var1) {
      if (this.names != null) {
         for(int var2 = 0; var2 < this.names.length; ++var2) {
            DerValue var3 = this.names[var2].findAttribute(var1);
            if (var3 != null) {
               return var3;
            }
         }
      }

      return null;
   }

   public DerValue findMostSpecificAttribute(ObjectIdentifier var1) {
      if (this.names != null) {
         for(int var2 = this.names.length - 1; var2 >= 0; --var2) {
            DerValue var3 = this.names[var2].findAttribute(var1);
            if (var3 != null) {
               return var3;
            }
         }
      }

      return null;
   }

   private void parseDER(DerInputStream var1) throws IOException {
      DerValue[] var2 = null;
      byte[] var3 = var1.toByteArray();

      try {
         var2 = var1.getSequence(5);
      } catch (IOException var6) {
         if (var3 == null) {
            var2 = null;
         } else {
            DerValue var5 = new DerValue((byte)48, var3);
            var3 = var5.toByteArray();
            var2 = (new DerInputStream(var3)).getSequence(5);
         }
      }

      if (var2 == null) {
         this.names = new RDN[0];
      } else {
         this.names = new RDN[var2.length];

         for(int var4 = 0; var4 < var2.length; ++var4) {
            this.names[var4] = new RDN(var2[var4]);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public void emit(DerOutputStream var1) throws IOException {
      this.encode(var1);
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();

      for(int var3 = 0; var3 < this.names.length; ++var3) {
         this.names[var3].encode(var2);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }

   public byte[] getEncodedInternal() throws IOException {
      if (this.encoded == null) {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();

         for(int var3 = 0; var3 < this.names.length; ++var3) {
            this.names[var3].encode(var2);
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.encoded = var1.toByteArray();
      }

      return this.encoded;
   }

   public byte[] getEncoded() throws IOException {
      return (byte[])this.getEncodedInternal().clone();
   }

   private void parseDN(String var1, Map<String, String> var2) throws IOException {
      if (var1 != null && var1.length() != 0) {
         ArrayList var3 = new ArrayList();
         int var4 = 0;
         int var7 = 0;
         String var8 = var1;
         int var9 = 0;
         int var10 = var1.indexOf(44);

         String var6;
         RDN var12;
         for(int var11 = var1.indexOf(59); var10 >= 0 || var11 >= 0; var11 = var8.indexOf(59, var9)) {
            int var5;
            if (var11 < 0) {
               var5 = var10;
            } else if (var10 < 0) {
               var5 = var11;
            } else {
               var5 = Math.min(var10, var11);
            }

            var7 += countQuotes(var8, var9, var5);
            if (var5 >= 0 && var7 != 1 && !escaped(var5, var9, var8)) {
               var6 = var8.substring(var4, var5);
               var12 = new RDN(var6, var2);
               var3.add(var12);
               var4 = var5 + 1;
               var7 = 0;
            }

            var9 = var5 + 1;
            var10 = var8.indexOf(44, var9);
         }

         var6 = var8.substring(var4);
         var12 = new RDN(var6, var2);
         var3.add(var12);
         Collections.reverse(var3);
         this.names = (RDN[])var3.toArray(new RDN[var3.size()]);
      } else {
         this.names = new RDN[0];
      }
   }

   private void parseRFC2253DN(String var1) throws IOException {
      if (var1.length() == 0) {
         this.names = new RDN[0];
      } else {
         ArrayList var2 = new ArrayList();
         int var3 = 0;
         int var5 = 0;

         String var4;
         RDN var7;
         for(int var6 = var1.indexOf(44); var6 >= 0; var6 = var1.indexOf(44, var5)) {
            if (var6 > 0 && !escaped(var6, var5, var1)) {
               var4 = var1.substring(var3, var6);
               var7 = new RDN(var4, "RFC2253");
               var2.add(var7);
               var3 = var6 + 1;
            }

            var5 = var6 + 1;
         }

         var4 = var1.substring(var3);
         var7 = new RDN(var4, "RFC2253");
         var2.add(var7);
         Collections.reverse(var2);
         this.names = (RDN[])var2.toArray(new RDN[var2.size()]);
      }
   }

   static int countQuotes(String var0, int var1, int var2) {
      int var3 = 0;

      for(int var4 = var1; var4 < var2; ++var4) {
         if (var0.charAt(var4) == '"' && var4 == var1 || var0.charAt(var4) == '"' && var0.charAt(var4 - 1) != '\\') {
            ++var3;
         }
      }

      return var3;
   }

   private static boolean escaped(int var0, int var1, String var2) {
      if (var0 == 1 && var2.charAt(var0 - 1) == '\\') {
         return true;
      } else if (var0 > 1 && var2.charAt(var0 - 1) == '\\' && var2.charAt(var0 - 2) != '\\') {
         return true;
      } else if (var0 > 1 && var2.charAt(var0 - 1) == '\\' && var2.charAt(var0 - 2) == '\\') {
         int var3 = 0;
         --var0;

         for(; var0 >= var1; --var0) {
            if (var2.charAt(var0) == '\\') {
               ++var3;
            }
         }

         return var3 % 2 != 0;
      } else {
         return false;
      }
   }

   private void generateDN() {
      if (this.names.length == 1) {
         this.dn = this.names[0].toString();
      } else {
         StringBuilder var1 = new StringBuilder(48);
         if (this.names != null) {
            for(int var2 = this.names.length - 1; var2 >= 0; --var2) {
               if (var2 != this.names.length - 1) {
                  var1.append(", ");
               }

               var1.append(this.names[var2].toString());
            }
         }

         this.dn = var1.toString();
      }
   }

   private String generateRFC1779DN(Map<String, String> var1) {
      if (this.names.length == 1) {
         return this.names[0].toRFC1779String(var1);
      } else {
         StringBuilder var2 = new StringBuilder(48);
         if (this.names != null) {
            for(int var3 = this.names.length - 1; var3 >= 0; --var3) {
               if (var3 != this.names.length - 1) {
                  var2.append(", ");
               }

               var2.append(this.names[var3].toRFC1779String(var1));
            }
         }

         return var2.toString();
      }
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else if (var1.getType() != 4) {
         var2 = -1;
      } else {
         X500Name var3 = (X500Name)var1;
         if (var3.equals(this)) {
            var2 = 0;
         } else if (var3.names.length == 0) {
            var2 = 2;
         } else if (this.names.length == 0) {
            var2 = 1;
         } else if (var3.isWithinSubtree(this)) {
            var2 = 1;
         } else if (this.isWithinSubtree(var3)) {
            var2 = 2;
         } else {
            var2 = 3;
         }
      }

      return var2;
   }

   private boolean isWithinSubtree(X500Name var1) {
      if (this == var1) {
         return true;
      } else if (var1 == null) {
         return false;
      } else if (var1.names.length == 0) {
         return true;
      } else if (this.names.length == 0) {
         return false;
      } else if (this.names.length < var1.names.length) {
         return false;
      } else {
         for(int var2 = 0; var2 < var1.names.length; ++var2) {
            if (!this.names[var2].equals(var1.names[var2])) {
               return false;
            }
         }

         return true;
      }
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      return this.names.length;
   }

   public X500Name commonAncestor(X500Name var1) {
      if (var1 == null) {
         return null;
      } else {
         int var2 = var1.names.length;
         int var3 = this.names.length;
         if (var3 != 0 && var2 != 0) {
            int var4 = var3 < var2 ? var3 : var2;

            int var5;
            for(var5 = 0; var5 < var4; ++var5) {
               if (!this.names[var5].equals(var1.names[var5])) {
                  if (var5 == 0) {
                     return null;
                  }
                  break;
               }
            }

            RDN[] var6 = new RDN[var5];

            for(int var7 = 0; var7 < var5; ++var7) {
               var6[var7] = this.names[var7];
            }

            X500Name var10 = null;

            try {
               var10 = new X500Name(var6);
               return var10;
            } catch (IOException var9) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public X500Principal asX500Principal() {
      if (this.x500Principal == null) {
         try {
            Object[] var1 = new Object[]{this};
            this.x500Principal = (X500Principal)principalConstructor.newInstance(var1);
         } catch (Exception var2) {
            throw new RuntimeException("Unexpected exception", var2);
         }
      }

      return this.x500Principal;
   }

   public static X500Name asX500Name(X500Principal var0) {
      try {
         X500Name var1 = (X500Name)principalField.get(var0);
         var1.x500Principal = var0;
         return var1;
      } catch (Exception var2) {
         throw new RuntimeException("Unexpected exception", var2);
      }
   }

   static {
      commonName_oid = ObjectIdentifier.newInternal(commonName_data);
      SERIALNUMBER_OID = ObjectIdentifier.newInternal(SERIALNUMBER_DATA);
      countryName_oid = ObjectIdentifier.newInternal(countryName_data);
      localityName_oid = ObjectIdentifier.newInternal(localityName_data);
      orgName_oid = ObjectIdentifier.newInternal(orgName_data);
      orgUnitName_oid = ObjectIdentifier.newInternal(orgUnitName_data);
      stateName_oid = ObjectIdentifier.newInternal(stateName_data);
      streetAddress_oid = ObjectIdentifier.newInternal(streetAddress_data);
      title_oid = ObjectIdentifier.newInternal(title_data);
      DNQUALIFIER_OID = ObjectIdentifier.newInternal(DNQUALIFIER_DATA);
      SURNAME_OID = ObjectIdentifier.newInternal(SURNAME_DATA);
      GIVENNAME_OID = ObjectIdentifier.newInternal(GIVENNAME_DATA);
      INITIALS_OID = ObjectIdentifier.newInternal(INITIALS_DATA);
      GENERATIONQUALIFIER_OID = ObjectIdentifier.newInternal(GENERATIONQUALIFIER_DATA);
      ipAddress_oid = ObjectIdentifier.newInternal(ipAddress_data);
      DOMAIN_COMPONENT_OID = ObjectIdentifier.newInternal(DOMAIN_COMPONENT_DATA);
      userid_oid = ObjectIdentifier.newInternal(userid_data);
      PrivilegedExceptionAction var0 = new PrivilegedExceptionAction<Object[]>() {
         public Object[] run() throws Exception {
            Class var1 = X500Principal.class;
            Class[] var2 = new Class[]{X500Name.class};
            Constructor var3 = var1.getDeclaredConstructor(var2);
            var3.setAccessible(true);
            Field var4 = var1.getDeclaredField("thisX500Name");
            var4.setAccessible(true);
            return new Object[]{var3, var4};
         }
      };

      try {
         Object[] var1 = (Object[]) AccessController.doPrivileged(var0);
         Constructor var2 = (Constructor)var1[0];
         principalConstructor = var2;
         principalField = (Field)var1[1];
      } catch (Exception var3) {
         throw new InternalError("Could not obtain X500Principal access", var3);
      }
   }
}
