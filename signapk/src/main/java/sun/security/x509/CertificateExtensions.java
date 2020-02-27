package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificateExtensions implements CertAttrSet<Extension> {
   public static final String IDENT = "x509.info.extensions";
   public static final String NAME = "extensions";
   private static final Debug debug = Debug.getInstance("x509");
   private Map<String, Extension> map = Collections.synchronizedMap(new TreeMap());
   private boolean unsupportedCritExt = false;
   private Map<String, Extension> unparseableExtensions;
   private static Class[] PARAMS = new Class[]{Boolean.class, Object.class};

   public CertificateExtensions() {
   }

   public CertificateExtensions(DerInputStream var1) throws IOException {
      this.init(var1);
   }

   private void init(DerInputStream var1) throws IOException {
      DerValue[] var2 = var1.getSequence(5);

      for(int var3 = 0; var3 < var2.length; ++var3) {
         Extension var4 = new Extension(var2[var3]);
         this.parseExtension(var4);
      }

   }

   private void parseExtension(Extension var1) throws IOException {
      try {
         Class var2 = OIDMap.getClass(var1.getExtensionId());
         if (var2 == null) {
            if (var1.isCritical()) {
               this.unsupportedCritExt = true;
            }

            if (this.map.put(var1.getExtensionId().toString(), var1) != null) {
               throw new IOException("Duplicate extensions not allowed");
            }
         } else {
            Constructor var9 = var2.getConstructor(PARAMS);
            Object[] var10 = new Object[]{var1.isCritical(), var1.getExtensionValue()};
            CertAttrSet var5 = (CertAttrSet)var9.newInstance(var10);
            if (this.map.put(var5.getName(), (Extension)var5) != null) {
               throw new IOException("Duplicate extensions not allowed");
            }
         }
      } catch (InvocationTargetException var6) {
         Throwable var3 = var6.getTargetException();
         if (!var1.isCritical()) {
            if (this.unparseableExtensions == null) {
               this.unparseableExtensions = new TreeMap();
            }

            this.unparseableExtensions.put(var1.getExtensionId().toString(), new UnparseableExtension(var1, var3));
            if (debug != null) {
               debug.println("Debug info only. Error parsing extension: " + var1);
               var3.printStackTrace();
               HexDumpEncoder var4 = new HexDumpEncoder();
               System.err.println(var4.encodeBuffer(var1.getExtensionValue()));
            }

         } else if (var3 instanceof IOException) {
            throw (IOException)var3;
         } else {
            throw new IOException(var3);
         }
      } catch (IOException var7) {
         throw var7;
      } catch (Exception var8) {
         throw new IOException(var8);
      }
   }

   public void encode(OutputStream var1) throws CertificateException, IOException {
      this.encode(var1, false);
   }

   public void encode(OutputStream var1, boolean var2) throws CertificateException, IOException {
      DerOutputStream var3 = new DerOutputStream();
      Collection var4 = this.map.values();
      Object[] var5 = var4.toArray();

      for(int var6 = 0; var6 < var5.length; ++var6) {
         if (var5[var6] instanceof CertAttrSet) {
            ((CertAttrSet)var5[var6]).encode(var3);
         } else {
            if (!(var5[var6] instanceof Extension)) {
               throw new CertificateException("Illegal extension object");
            }

            ((Extension)var5[var6]).encode(var3);
         }
      }

      DerOutputStream var8 = new DerOutputStream();
      var8.write((byte)48, (DerOutputStream)var3);
      DerOutputStream var7;
      if (!var2) {
         var7 = new DerOutputStream();
         var7.write(DerValue.createTag((byte)-128, true, (byte)3), var8);
      } else {
         var7 = var8;
      }

      var1.write(var7.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var2 instanceof Extension) {
         this.map.put(var1, (Extension)var2);
      } else {
         throw new IOException("Unknown extension type.");
      }
   }

   public Extension get(String var1) throws IOException {
      Extension var2 = (Extension)this.map.get(var1);
      if (var2 == null) {
         throw new IOException("No extension found with name " + var1);
      } else {
         return var2;
      }
   }

   Extension getExtension(String var1) {
      return (Extension)this.map.get(var1);
   }

   public void delete(String var1) throws IOException {
      Object var2 = this.map.get(var1);
      if (var2 == null) {
         throw new IOException("No extension found with name " + var1);
      } else {
         this.map.remove(var1);
      }
   }

   public String getNameByOid(ObjectIdentifier var1) throws IOException {
      Iterator var2 = this.map.keySet().iterator();

      String var3;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         var3 = (String)var2.next();
      } while(!((Extension)this.map.get(var3)).getExtensionId().equals((Object)var1));

      return var3;
   }

   public Enumeration<Extension> getElements() {
      return Collections.enumeration(this.map.values());
   }

   public Collection<Extension> getAllExtensions() {
      return this.map.values();
   }

   public Map<String, Extension> getUnparseableExtensions() {
      return this.unparseableExtensions == null ? Collections.emptyMap() : this.unparseableExtensions;
   }

   public String getName() {
      return "extensions";
   }

   public boolean hasUnsupportedCriticalExtension() {
      return this.unsupportedCritExt;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof CertificateExtensions)) {
         return false;
      } else {
         Collection var2 = ((CertificateExtensions)var1).getAllExtensions();
         Object[] var3 = var2.toArray();
         int var4 = var3.length;
         if (var4 != this.map.size()) {
            return false;
         } else {
            String var7 = null;

            for(int var8 = 0; var8 < var4; ++var8) {
               if (var3[var8] instanceof CertAttrSet) {
                  var7 = ((CertAttrSet)var3[var8]).getName();
               }

               Extension var5 = (Extension)var3[var8];
               if (var7 == null) {
                  var7 = var5.getExtensionId().toString();
               }

               Extension var6 = (Extension)this.map.get(var7);
               if (var6 == null) {
                  return false;
               }

               if (!var6.equals(var5)) {
                  return false;
               }
            }

            return this.getUnparseableExtensions().equals(((CertificateExtensions)var1).getUnparseableExtensions());
         }
      }
   }

   public int hashCode() {
      return this.map.hashCode() + this.getUnparseableExtensions().hashCode();
   }

   public String toString() {
      return this.map.toString();
   }
}
