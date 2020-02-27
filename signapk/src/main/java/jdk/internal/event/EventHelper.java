package jdk.internal.event;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import sun.util.logging.PlatformLogger;

public final class EventHelper {
   private static final PlatformLogger.Level LOG_LEVEL;
   private static final String SECURITY_LOGGER_NAME = "jdk.event.security";
   private static final PlatformLogger SECURITY_LOGGER;
   private static final boolean LOGGING_SECURITY;

   public static void logTLSHandshakeEvent(Instant var0, String var1, int var2, String var3, String var4, long var5) {
      String var7 = getDurationString(var0);
      SECURITY_LOGGER.fine(var7 + " TLSHandshake: {0}:{1}, {2}, {3}, {4}", var1, var2, var4, var3, var5);
   }

   public static void logSecurityPropertyEvent(String var0, String var1) {
      SECURITY_LOGGER.fine("SecurityPropertyModification: key:{0}, value:{1}", var0, var1);
   }

   public static void logX509ValidationEvent(int var0, int[] var1) {
      String var2 = (String) IntStream.of(var1).mapToObj(Integer::toString).collect(Collectors.joining(", "));
      SECURITY_LOGGER.fine("ValidationChain: {0}, {1}", var0, var2);
   }

   public static void logX509CertificateEvent(String var0, String var1, String var2, String var3, String var4, int var5, long var6, long var8, long var10) {
      SECURITY_LOGGER.fine("X509Certificate: Alg:{0}, Serial:{1}, Subject:{2}, Issuer:{3}, Key type:{4}, Length:{5}, Cert Id:{6}, Valid from:{7}, Valid until:{8}", var0, var1, var2, var3, var4, var5, var6, new Date(var8), new Date(var10));
   }

   private static String getDurationString(Instant var0) {
      if (var0 != null) {
         Duration var1 = Duration.between(var0, Instant.now());
         long var2 = var1.toNanos() / 1000L;
         return var2 < 1000000L ? "duration = " + (double)var2 / 1000.0D + " ms:" : "duration = " + (double)(var2 / 1000L) / 1000.0D + " s:";
      } else {
         return "";
      }
   }

   public static boolean isLoggingSecurity() {
      return LOGGING_SECURITY;
   }

   static {
      LOG_LEVEL = PlatformLogger.Level.FINE;
      SECURITY_LOGGER = PlatformLogger.getLogger("jdk.event.security");
      LOGGING_SECURITY = SECURITY_LOGGER.isLoggable(LOG_LEVEL);
   }
}
