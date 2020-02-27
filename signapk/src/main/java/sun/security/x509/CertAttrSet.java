package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public interface CertAttrSet<T> {
   String toString();

   void encode(OutputStream var1) throws CertificateException, IOException;

   void set(String var1, Object var2) throws CertificateException, IOException;

   Object get(String var1) throws CertificateException, IOException;

   void delete(String var1) throws CertificateException, IOException;

   Enumeration<T> getElements();

   String getName();
}
