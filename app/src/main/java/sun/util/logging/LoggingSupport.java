package sun.util.logging;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.List;

public class LoggingSupport {
   private static final LoggingProxy proxy = (LoggingProxy) AccessController.doPrivileged(new PrivilegedAction<LoggingProxy>() {
      public LoggingProxy run() {
         try {
            Class var1 = Class.forName("java.util.logging.LoggingProxyImpl", true, (ClassLoader)null);
            Field var2 = var1.getDeclaredField("INSTANCE");
            var2.setAccessible(true);
            return (LoggingProxy)var2.get((Object)null);
         } catch (ClassNotFoundException var3) {
            return null;
         } catch (NoSuchFieldException var4) {
            throw new AssertionError(var4);
         } catch (IllegalAccessException var5) {
            throw new AssertionError(var5);
         }
      }
   });
   private static final String DEFAULT_FORMAT = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
   private static final String FORMAT_PROP_KEY = "java.util.logging.SimpleFormatter.format";

   private LoggingSupport() {
   }

   public static boolean isAvailable() {
      return proxy != null;
   }

   private static void ensureAvailable() {
      if (proxy == null) {
         throw new AssertionError("Should not here");
      }
   }

   public static List<String> getLoggerNames() {
      ensureAvailable();
      return proxy.getLoggerNames();
   }

   public static String getLoggerLevel(String var0) {
      ensureAvailable();
      return proxy.getLoggerLevel(var0);
   }

   public static void setLoggerLevel(String var0, String var1) {
      ensureAvailable();
      proxy.setLoggerLevel(var0, var1);
   }

   public static String getParentLoggerName(String var0) {
      ensureAvailable();
      return proxy.getParentLoggerName(var0);
   }

   public static Object getLogger(String var0) {
      ensureAvailable();
      return proxy.getLogger(var0);
   }

   public static Object getLevel(Object var0) {
      ensureAvailable();
      return proxy.getLevel(var0);
   }

   public static void setLevel(Object var0, Object var1) {
      ensureAvailable();
      proxy.setLevel(var0, var1);
   }

   public static boolean isLoggable(Object var0, Object var1) {
      ensureAvailable();
      return proxy.isLoggable(var0, var1);
   }

   public static void log(Object var0, Object var1, String var2) {
      ensureAvailable();
      proxy.log(var0, var1, var2);
   }

   public static void log(Object var0, Object var1, String var2, Throwable var3) {
      ensureAvailable();
      proxy.log(var0, var1, var2, var3);
   }

   public static void log(Object var0, Object var1, String var2, Object... var3) {
      ensureAvailable();
      proxy.log(var0, var1, var2, var3);
   }

   public static Object parseLevel(String var0) {
      ensureAvailable();
      return proxy.parseLevel(var0);
   }

   public static String getLevelName(Object var0) {
      ensureAvailable();
      return proxy.getLevelName(var0);
   }

   public static int getLevelValue(Object var0) {
      ensureAvailable();
      return proxy.getLevelValue(var0);
   }

   public static String getSimpleFormat() {
      return getSimpleFormat(true);
   }

   static String getSimpleFormat(boolean var0) {
      String var1 = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
         public String run() {
            return System.getProperty("java.util.logging.SimpleFormatter.format");
         }
      });
      if (var0 && proxy != null && var1 == null) {
         var1 = proxy.getProperty("java.util.logging.SimpleFormatter.format");
      }

      if (var1 != null) {
         try {
            String.format(var1, new Date(), "", "", "", "", "");
         } catch (IllegalArgumentException var3) {
            var1 = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
         }
      } else {
         var1 = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
      }

      return var1;
   }
}
