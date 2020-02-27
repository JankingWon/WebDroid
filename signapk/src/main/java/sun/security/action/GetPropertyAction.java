package sun.security.action;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class GetPropertyAction implements PrivilegedAction<String> {
   private String theProp;
   private String defaultVal;

   public GetPropertyAction(String var1) {
      this.theProp = var1;
   }

   public GetPropertyAction(String var1, String var2) {
      this.theProp = var1;
      this.defaultVal = var2;
   }

   public String run() {
      String var1 = System.getProperty(this.theProp);
      return var1 == null ? this.defaultVal : var1;
   }

   public static String privilegedGetProperty(String var0) {
      return System.getSecurityManager() == null ? System.getProperty(var0) : (String) AccessController.doPrivileged((PrivilegedAction)(new GetPropertyAction(var0)));
   }

   public static String privilegedGetProperty(String var0, String var1) {
      return System.getSecurityManager() == null ? System.getProperty(var0, var1) : (String) AccessController.doPrivileged((PrivilegedAction)(new GetPropertyAction(var0, var1)));
   }
}
