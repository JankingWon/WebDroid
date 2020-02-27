package sun.misc;

/*import java.io.Console;
import java.io.FileDescriptor;
import java.io.ObjectInputStream;
import java.net.HttpCookie;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import javax.crypto.SealedObject;*/

public class SharedSecrets {
/*   private static final Unsafe unsafe = Unsafe.getUnsafe();
   private static JavaUtilJarAccess javaUtilJarAccess;*/
   private static JavaLangAccess javaLangAccess;
/*
   private static JavaLangRefAccess javaLangRefAccess;
   private static JavaIOAccess javaIOAccess;
   private static JavaNetAccess javaNetAccess;
   private static JavaNetHttpCookieAccess javaNetHttpCookieAccess;
   private static JavaNioAccess javaNioAccess;
   private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;
   private static JavaSecurityProtectionDomainAccess javaSecurityProtectionDomainAccess;
   private static JavaSecurityAccess javaSecurityAccess;
   private static JavaUtilZipFileAccess javaUtilZipFileAccess;
   private static JavaAWTAccess javaAWTAccess;
   private static JavaOISAccess javaOISAccess;
   private static JavaxCryptoSealedObjectAccess javaxCryptoSealedObjectAccess;
   private static JavaObjectInputStreamReadString javaObjectInputStreamReadString;
   private static JavaObjectInputStreamAccess javaObjectInputStreamAccess;
*/

/*
   public static JavaUtilJarAccess javaUtilJarAccess() {
      if (javaUtilJarAccess == null) {
         unsafe.ensureClassInitialized(JarFile.class);
      }

      return javaUtilJarAccess;
   }

   public static void setJavaUtilJarAccess(JavaUtilJarAccess var0) {
      javaUtilJarAccess = var0;
   }

   public static void setJavaLangAccess(JavaLangAccess var0) {
      javaLangAccess = var0;
   }
*/

   public static JavaLangAccess getJavaLangAccess() {
      return javaLangAccess;
   }

/*   public static void setJavaLangRefAccess(JavaLangRefAccess var0) {
      javaLangRefAccess = var0;
   }

   public static JavaLangRefAccess getJavaLangRefAccess() {
      return javaLangRefAccess;
   }

   public static void setJavaNetAccess(JavaNetAccess var0) {
      javaNetAccess = var0;
   }

   public static JavaNetAccess getJavaNetAccess() {
      return javaNetAccess;
   }

   public static void setJavaNetHttpCookieAccess(JavaNetHttpCookieAccess var0) {
      javaNetHttpCookieAccess = var0;
   }

   public static JavaNetHttpCookieAccess getJavaNetHttpCookieAccess() {
      if (javaNetHttpCookieAccess == null) {
         unsafe.ensureClassInitialized(HttpCookie.class);
      }

      return javaNetHttpCookieAccess;
   }

   public static void setJavaNioAccess(JavaNioAccess var0) {
      javaNioAccess = var0;
   }

   public static JavaNioAccess getJavaNioAccess() {
      if (javaNioAccess == null) {
         unsafe.ensureClassInitialized(ByteOrder.class);
      }

      return javaNioAccess;
   }

   public static void setJavaIOAccess(JavaIOAccess var0) {
      javaIOAccess = var0;
   }

   public static JavaIOAccess getJavaIOAccess() {
      if (javaIOAccess == null) {
         unsafe.ensureClassInitialized(Console.class);
      }

      return javaIOAccess;
   }

   public static void setJavaIOFileDescriptorAccess(JavaIOFileDescriptorAccess var0) {
      javaIOFileDescriptorAccess = var0;
   }

   public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
      if (javaIOFileDescriptorAccess == null) {
         unsafe.ensureClassInitialized(FileDescriptor.class);
      }

      return javaIOFileDescriptorAccess;
   }

   public static void setJavaOISAccess(JavaOISAccess var0) {
      javaOISAccess = var0;
   }

   public static JavaOISAccess getJavaOISAccess() {
      if (javaOISAccess == null) {
         unsafe.ensureClassInitialized(ObjectInputStream.class);
      }

      return javaOISAccess;
   }

   public static void setJavaSecurityProtectionDomainAccess(JavaSecurityProtectionDomainAccess var0) {
      javaSecurityProtectionDomainAccess = var0;
   }

   public static JavaSecurityProtectionDomainAccess getJavaSecurityProtectionDomainAccess() {
      if (javaSecurityProtectionDomainAccess == null) {
         unsafe.ensureClassInitialized(ProtectionDomain.class);
      }

      return javaSecurityProtectionDomainAccess;
   }

   public static void setJavaSecurityAccess(JavaSecurityAccess var0) {
      javaSecurityAccess = var0;
   }

   public static JavaSecurityAccess getJavaSecurityAccess() {
      if (javaSecurityAccess == null) {
         unsafe.ensureClassInitialized(AccessController.class);
      }

      return javaSecurityAccess;
   }

   public static JavaUtilZipFileAccess getJavaUtilZipFileAccess() {
      if (javaUtilZipFileAccess == null) {
         unsafe.ensureClassInitialized(ZipFile.class);
      }

      return javaUtilZipFileAccess;
   }

   public static void setJavaUtilZipFileAccess(JavaUtilZipFileAccess var0) {
      javaUtilZipFileAccess = var0;
   }

   public static void setJavaAWTAccess(JavaAWTAccess var0) {
      javaAWTAccess = var0;
   }

   public static JavaAWTAccess getJavaAWTAccess() {
      return javaAWTAccess == null ? null : javaAWTAccess;
   }

   public static JavaObjectInputStreamReadString getJavaObjectInputStreamReadString() {
      if (javaObjectInputStreamReadString == null) {
         unsafe.ensureClassInitialized(ObjectInputStream.class);
      }

      return javaObjectInputStreamReadString;
   }

   public static void setJavaObjectInputStreamReadString(JavaObjectInputStreamReadString var0) {
      javaObjectInputStreamReadString = var0;
   }

   public static JavaObjectInputStreamAccess getJavaObjectInputStreamAccess() {
      if (javaObjectInputStreamAccess == null) {
         unsafe.ensureClassInitialized(ObjectInputStream.class);
      }

      return javaObjectInputStreamAccess;
   }

   public static void setJavaObjectInputStreamAccess(JavaObjectInputStreamAccess var0) {
      javaObjectInputStreamAccess = var0;
   }

   public static void setJavaxCryptoSealedObjectAccess(JavaxCryptoSealedObjectAccess var0) {
      javaxCryptoSealedObjectAccess = var0;
   }

   public static JavaxCryptoSealedObjectAccess getJavaxCryptoSealedObjectAccess() {
      if (javaxCryptoSealedObjectAccess == null) {
         unsafe.ensureClassInitialized(SealedObject.class);
      }

      return javaxCryptoSealedObjectAccess;
   }*/
}
