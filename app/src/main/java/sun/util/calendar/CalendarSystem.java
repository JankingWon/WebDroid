package sun.util.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.security.action.GetPropertyAction;

public abstract class CalendarSystem {
   private static volatile boolean initialized = false;
   private static ConcurrentMap<String, String> names;
   private static ConcurrentMap<String, CalendarSystem> calendars;
   private static final String PACKAGE_NAME = "sun.util.calendar.";
   private static final String[] namePairs = new String[]{"gregorian", "Gregorian", "japanese", "LocalGregorianCalendar", "julian", "JulianCalendar"};
   private static final Gregorian GREGORIAN_INSTANCE = new Gregorian();

   private static void initNames() {
      ConcurrentHashMap var0 = new ConcurrentHashMap();
      StringBuilder var1 = new StringBuilder();

      for(int var2 = 0; var2 < namePairs.length; var2 += 2) {
         var1.setLength(0);
         String var3 = var1.append("sun.util.calendar.").append(namePairs[var2 + 1]).toString();
         var0.put(namePairs[var2], var3);
      }

      Class var6 = CalendarSystem.class;
      synchronized(CalendarSystem.class) {
         if (!initialized) {
            names = var0;
            calendars = new ConcurrentHashMap();
            initialized = true;
         }

      }
   }

   public static Gregorian getGregorianCalendar() {
      return GREGORIAN_INSTANCE;
   }

   public static CalendarSystem forName(String var0) {
      if ("gregorian".equals(var0)) {
         return GREGORIAN_INSTANCE;
      } else {
         if (!initialized) {
            initNames();
         }

         CalendarSystem var1 = (CalendarSystem)calendars.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            String var2 = (String)names.get(var0);
            if (var2 == null) {
               return null;
            } else {
               CalendarSystem var5;
               if (var2.endsWith("LocalGregorianCalendar")) {
                  var5 = LocalGregorianCalendar.getLocalGregorianCalendar(var0);
               } else {
                  try {
                     Class var3 = Class.forName(var2);
                     var5 = (CalendarSystem)var3.newInstance();
                  } catch (Exception var4) {
                     throw new InternalError(var4);
                  }
               }

               if (var5 == null) {
                  return null;
               } else {
                  CalendarSystem var6 = (CalendarSystem)calendars.putIfAbsent(var0, var5);
                  return (CalendarSystem)(var6 == null ? var5 : var6);
               }
            }
         }
      }
   }

   public static Properties getCalendarProperties() throws IOException {
      Properties var0 = null;

      try {
         String var1 = (String) AccessController.doPrivileged((PrivilegedAction)(new GetPropertyAction("java.home")));
         final String var4 = var1 + File.separator + "lib" + File.separator + "calendars.properties";
         var0 = (Properties) AccessController.doPrivileged(new PrivilegedExceptionAction<Properties>() {
            public Properties run() throws IOException {
               Properties var1 = new Properties();
               FileInputStream var2 = new FileInputStream(var4);
               Throwable var3 = null;

               try {
                  var1.load((InputStream)var2);
               } catch (Throwable var12) {
                  var3 = var12;
                  throw var12;
               } finally {
                  if (var2 != null) {
                     if (var3 != null) {
                        try {
                           var2.close();
                        } catch (Throwable var11) {
                           var3.addSuppressed(var11);
                        }
                     } else {
                        var2.close();
                     }
                  }

               }

               return var1;
            }
         });
         return var0;
      } catch (PrivilegedActionException var3) {
         Throwable var2 = var3.getCause();
         if (var2 instanceof IOException) {
            throw (IOException)var2;
         } else if (var2 instanceof IllegalArgumentException) {
            throw (IllegalArgumentException)var2;
         } else {
            throw new InternalError(var2);
         }
      }
   }

   public abstract String getName();

   public abstract CalendarDate getCalendarDate();

   public abstract CalendarDate getCalendarDate(long var1);

   public abstract CalendarDate getCalendarDate(long var1, CalendarDate var3);

   public abstract CalendarDate getCalendarDate(long var1, TimeZone var3);

   public abstract CalendarDate newCalendarDate();

   public abstract CalendarDate newCalendarDate(TimeZone var1);

   public abstract long getTime(CalendarDate var1);

   public abstract int getYearLength(CalendarDate var1);

   public abstract int getYearLengthInMonths(CalendarDate var1);

   public abstract int getMonthLength(CalendarDate var1);

   public abstract int getWeekLength();

   public abstract Era getEra(String var1);

   public abstract Era[] getEras();

   public abstract void setEra(CalendarDate var1, String var2);

   public abstract CalendarDate getNthDayOfWeek(int var1, int var2, CalendarDate var3);

   public abstract CalendarDate setTimeOfDay(CalendarDate var1, int var2);

   public abstract boolean validate(CalendarDate var1);

   public abstract boolean normalize(CalendarDate var1);
}
