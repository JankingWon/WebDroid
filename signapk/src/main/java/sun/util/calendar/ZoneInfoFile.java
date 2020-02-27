package sun.util.calendar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import sun.security.action.GetPropertyAction;

public final class ZoneInfoFile {
   private static String versionId;
   private static final Map<String, ZoneInfo> zones = new ConcurrentHashMap();
   private static Map<String, String> aliases = new HashMap();
   private static byte[][] ruleArray;
   private static String[] regions;
   private static int[] indices;
   private static final boolean USE_OLDMAPPING;
   private static String[][] oldMappings = new String[][]{{"ACT", "Australia/Darwin"}, {"AET", "Australia/Sydney"}, {"AGT", "America/Argentina/Buenos_Aires"}, {"ART", "Africa/Cairo"}, {"AST", "America/Anchorage"}, {"BET", "America/Sao_Paulo"}, {"BST", "Asia/Dhaka"}, {"CAT", "Africa/Harare"}, {"CNT", "America/St_Johns"}, {"CST", "America/Chicago"}, {"CTT", "Asia/Shanghai"}, {"EAT", "Africa/Addis_Ababa"}, {"ECT", "Europe/Paris"}, {"IET", "America/Indiana/Indianapolis"}, {"IST", "Asia/Kolkata"}, {"JST", "Asia/Tokyo"}, {"MIT", "Pacific/Apia"}, {"NET", "Asia/Yerevan"}, {"NST", "Pacific/Auckland"}, {"PLT", "Asia/Karachi"}, {"PNT", "America/Phoenix"}, {"PRT", "America/Puerto_Rico"}, {"PST", "America/Los_Angeles"}, {"SST", "Pacific/Guadalcanal"}, {"VST", "Asia/Ho_Chi_Minh"}};
   private static final long UTC1900 = -2208988800L;
   private static final long UTC2037 = 2145916799L;
   private static final long LDT2037 = 2114380800L;
   private static final long CURRT;
   static final int SECONDS_PER_DAY = 86400;
   static final int DAYS_PER_CYCLE = 146097;
   static final long DAYS_0000_TO_1970 = 719528L;
   private static final int[] toCalendarDOW;
   private static final int[] toSTZTime;
   private static final long OFFSET_MASK = 15L;
   private static final long DST_MASK = 240L;
   private static final int DST_NSHIFT = 4;
   private static final int TRANSITION_NSHIFT = 12;
   private static final int LASTYEAR = 2037;

   public static String[] getZoneIds() {
      int var0 = regions.length + oldMappings.length;
      if (!USE_OLDMAPPING) {
         var0 += 3;
      }

      String[] var1 = (String[]) Arrays.copyOf((Object[])regions, var0);
      int var2 = regions.length;
      if (!USE_OLDMAPPING) {
         var1[var2++] = "EST";
         var1[var2++] = "HST";
         var1[var2++] = "MST";
      }

      for(int var3 = 0; var3 < oldMappings.length; ++var3) {
         var1[var2++] = oldMappings[var3][0];
      }

      return var1;
   }

   public static String[] getZoneIds(int var0) {
      ArrayList var1 = new ArrayList();
      String[] var2 = getZoneIds();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String var5 = var2[var4];
         ZoneInfo var6 = getZoneInfo(var5);
         if (var6.getRawOffset() == var0) {
            var1.add(var5);
         }
      }

      var2 = (String[])var1.toArray(new String[var1.size()]);
      Arrays.sort((Object[])var2);
      return var2;
   }

   public static ZoneInfo getZoneInfo(String var0) {
      if (var0 == null) {
         return null;
      } else {
         ZoneInfo var1 = getZoneInfo0(var0);
         if (var1 != null) {
            var1 = (ZoneInfo)var1.clone();
            var1.setID(var0);
         }

         return var1;
      }
   }

   private static ZoneInfo getZoneInfo0(String var0) {
      try {
         ZoneInfo var1 = (ZoneInfo)zones.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            String var2 = var0;
            if (aliases.containsKey(var0)) {
               var2 = (String)aliases.get(var0);
            }

            int var3 = Arrays.binarySearch(regions, var2);
            if (var3 < 0) {
               return null;
            } else {
               byte[] var4 = ruleArray[indices[var3]];
               DataInputStream var5 = new DataInputStream(new ByteArrayInputStream(var4));
               var1 = getZoneInfo(var5, var2);
               zones.put(var0, var1);
               return var1;
            }
         }
      } catch (Exception var6) {
         throw new RuntimeException("Invalid binary time-zone data: TZDB:" + var0 + ", version: " + versionId, var6);
      }
   }

   public static Map<String, String> getAliasMap() {
      return Collections.unmodifiableMap(aliases);
   }

   public static String getVersion() {
      return versionId;
   }

   public static ZoneInfo getCustomTimeZone(String var0, int var1) {
      String var2 = toCustomID(var1);
      return new ZoneInfo(var2, var1);
   }

   public static String toCustomID(int var0) {
      int var2 = var0 / '\uea60';
      char var1;
      if (var2 >= 0) {
         var1 = '+';
      } else {
         var1 = '-';
         var2 = -var2;
      }

      int var3 = var2 / 60;
      int var4 = var2 % 60;
      char[] var5 = new char[]{'G', 'M', 'T', var1, '0', '0', ':', '0', '0'};
      if (var3 >= 10) {
         var5[4] = (char)(var5[4] + var3 / 10);
      }

      var5[5] = (char)(var5[5] + var3 % 10);
      if (var4 != 0) {
         var5[7] = (char)(var5[7] + var4 / 10);
         var5[8] = (char)(var5[8] + var4 % 10);
      }

      return new String(var5);
   }

   private ZoneInfoFile() {
   }

   private static void addOldMapping() {
      String[][] var0 = oldMappings;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         String[] var3 = var0[var2];
         aliases.put(var3[0], var3[1]);
      }

      if (USE_OLDMAPPING) {
         aliases.put("EST", "America/New_York");
         aliases.put("MST", "America/Denver");
         aliases.put("HST", "Pacific/Honolulu");
      } else {
         zones.put("EST", new ZoneInfo("EST", -18000000));
         zones.put("MST", new ZoneInfo("MST", -25200000));
         zones.put("HST", new ZoneInfo("HST", -36000000));
      }

   }

   public static boolean useOldMapping() {
      return USE_OLDMAPPING;
   }

   private static void load(DataInputStream var0) throws ClassNotFoundException, IOException {
      if (var0.readByte() != 1) {
         throw new StreamCorruptedException("File format not recognised");
      } else {
         String var1 = var0.readUTF();
         if (!"TZDB".equals(var1)) {
            throw new StreamCorruptedException("File format not recognised");
         } else {
            short var2 = var0.readShort();

            for(int var3 = 0; var3 < var2; ++var3) {
               versionId = var0.readUTF();
            }

            short var11 = var0.readShort();
            String[] var4 = new String[var11];

            for(int var5 = 0; var5 < var11; ++var5) {
               var4[var5] = var0.readUTF();
            }

            short var12 = var0.readShort();
            ruleArray = new byte[var12][];

            int var6;
            for(var6 = 0; var6 < var12; ++var6) {
               byte[] var7 = new byte[var0.readShort()];
               var0.readFully(var7);
               ruleArray[var6] = var7;
            }

            for(var6 = 0; var6 < var2; ++var6) {
               var11 = var0.readShort();
               regions = new String[var11];
               indices = new int[var11];

               for(int var13 = 0; var13 < var11; ++var13) {
                  regions[var13] = var4[var0.readShort()];
                  indices[var13] = var0.readShort();
               }
            }

            zones.remove("ROC");

            for(var6 = 0; var6 < var2; ++var6) {
               short var14 = var0.readShort();
               aliases.clear();

               for(int var8 = 0; var8 < var14; ++var8) {
                  String var9 = var4[var0.readShort()];
                  String var10 = var4[var0.readShort()];
                  aliases.put(var9, var10);
               }
            }

            addOldMapping();
         }
      }
   }

   public static ZoneInfo getZoneInfo(DataInput var0, String var1) throws Exception {
      byte var2 = var0.readByte();
      int var3 = var0.readInt();
      long[] var4 = new long[var3];

      for(int var5 = 0; var5 < var3; ++var5) {
         var4[var5] = readEpochSec(var0);
      }

      int[] var12 = new int[var3 + 1];

      int var6;
      for(var6 = 0; var6 < var12.length; ++var6) {
         var12[var6] = readOffset(var0);
      }

      var6 = var0.readInt();
      long[] var7 = new long[var6];

      for(int var8 = 0; var8 < var6; ++var8) {
         var7[var8] = readEpochSec(var0);
      }

      int[] var13 = new int[var6 + 1];

      for(int var9 = 0; var9 < var13.length; ++var9) {
         var13[var9] = readOffset(var0);
      }

      byte var14 = var0.readByte();
      ZoneOffsetTransitionRule[] var10 = new ZoneOffsetTransitionRule[var14];

      for(int var11 = 0; var11 < var14; ++var11) {
         var10[var11] = new ZoneOffsetTransitionRule(var0);
      }

      return getZoneInfo(var1, var4, var12, var7, var13, var10);
   }

   public static int readOffset(DataInput var0) throws IOException {
      byte var1 = var0.readByte();
      return var1 == 127 ? var0.readInt() : var1 * 900;
   }

   static long readEpochSec(DataInput var0) throws IOException {
      int var1 = var0.readByte() & 255;
      if (var1 == 255) {
         return var0.readLong();
      } else {
         int var2 = var0.readByte() & 255;
         int var3 = var0.readByte() & 255;
         long var4 = (long)((var1 << 16) + (var2 << 8) + var3);
         return var4 * 900L - 4575744000L;
      }
   }

   private static ZoneInfo getZoneInfo(String var0, long[] var1, int[] var2, long[] var3, int[] var4, ZoneOffsetTransitionRule[] var5) {
      boolean var6 = false;
      int var7 = 0;
      int var8 = 0;
      int[] var9 = null;
      boolean var10 = false;
      int var32;
      if (var1.length > 0) {
         var32 = var2[var2.length - 1] * 1000;
         var10 = var1[var1.length - 1] > CURRT;
      } else {
         var32 = var2[0] * 1000;
      }

      long[] var11 = null;
      int[] var12 = null;
      int var13 = 0;
      int var14 = 0;
      if (var3.length != 0) {
         var11 = new long[250];
         var12 = new int[100];
         int var15 = getYear(var3[var3.length - 1], var4[var3.length - 1]);
         int var16 = 0;

         int var17;
         for(var17 = 1; var16 < var3.length && var3[var16] < -2208988800L; ++var16) {
         }

         if (var16 < var3.length) {
            if (var16 < var3.length) {
               var12[0] = var2[var2.length - 1] * 1000;
               var13 = 1;
            }

            var13 = addTrans(var11, var14++, var12, var13, -2208988800L, var4[var16], getStandardOffset(var1, var2, -2208988800L));
         }

         long var18;
         while(var16 < var3.length) {
            var18 = var3[var16];
            if (var18 > 2145916799L) {
               var15 = 2037;
               break;
            }

            for(; var17 < var1.length; ++var17) {
               long var20 = var1[var17];
               if (var20 >= -2208988800L) {
                  if (var20 > var18) {
                     break;
                  }

                  if (var20 < var18) {
                     if (var13 + 2 >= var12.length) {
                        var12 = Arrays.copyOf(var12, var12.length + 100);
                     }

                     if (var14 + 1 >= var11.length) {
                        var11 = Arrays.copyOf(var11, var11.length + 100);
                     }

                     var13 = addTrans(var11, var14++, var12, var13, var20, var4[var16], var2[var17 + 1]);
                  }
               }
            }

            if (var13 + 2 >= var12.length) {
               var12 = Arrays.copyOf(var12, var12.length + 100);
            }

            if (var14 + 1 >= var11.length) {
               var11 = Arrays.copyOf(var11, var11.length + 100);
            }

            var13 = addTrans(var11, var14++, var12, var13, var18, var4[var16 + 1], getStandardOffset(var1, var2, var18));
            ++var16;
         }

         int var21;
         int var36;
         for(; var17 < var1.length; ++var17) {
            var18 = var1[var17];
            if (var18 >= -2208988800L) {
               var36 = var4[var16];
               var21 = indexOf(var12, 0, var13, var36);
               if (var21 == var13) {
                  ++var13;
               }

               var11[var14++] = var18 * 1000L << 12 | (long)var21 & 15L;
            }
         }

         long var38;
         if (var5.length <= 1) {
            if (var14 > 0) {
               if (var15 < 2037) {
                  var18 = 2114380800L - (long)(var32 / 1000);
                  var36 = indexOf(var12, 0, var13, var32 / 1000);
                  if (var36 == var13) {
                     ++var13;
                  }

                  var11[var14++] = var18 * 1000L << 12 | (long)var36 & 15L;
               } else if (var3.length > 2) {
                  int var37 = var3.length;
                  var38 = var3[var37 - 2];
                  var21 = var4[var37 - 2 + 1];
                  int var43 = getStandardOffset(var1, var2, var38);
                  long var23 = var3[var37 - 1];
                  int var25 = var4[var37 - 1 + 1];
                  int var26 = getStandardOffset(var1, var2, var23);
                  if (var21 > var43 && var25 == var26) {
                     var37 = var3.length - 2;
                     ZoneOffset var27 = ZoneOffset.ofTotalSeconds(var4[var37]);
                     ZoneOffset var28 = ZoneOffset.ofTotalSeconds(var4[var37 + 1]);
                     LocalDateTime var29 = LocalDateTime.ofEpochSecond(var3[var37], 0, var27);
                     LocalDateTime var30;
                     if (var28.getTotalSeconds() > var27.getTotalSeconds()) {
                        var30 = var29;
                     } else {
                        var30 = var29.plusSeconds((long)(var4[var37 + 1] - var4[var37]));
                     }

                     var37 = var3.length - 1;
                     var27 = ZoneOffset.ofTotalSeconds(var4[var37]);
                     var28 = ZoneOffset.ofTotalSeconds(var4[var37 + 1]);
                     var29 = LocalDateTime.ofEpochSecond(var3[var37], 0, var27);
                     LocalDateTime var31;
                     if (var28.getTotalSeconds() > var27.getTotalSeconds()) {
                        var31 = var29.plusSeconds((long)(var4[var37 + 1] - var4[var37]));
                     } else {
                        var31 = var29;
                     }

                     var9 = new int[]{var30.getMonthValue() - 1, var30.getDayOfMonth(), 0, var30.toLocalTime().toSecondOfDay() * 1000, 0, var31.getMonthValue() - 1, var31.getDayOfMonth(), 0, var31.toLocalTime().toSecondOfDay() * 1000, 0};
                     var7 = (var21 - var43) * 1000;
                  }
               }
            }
         } else {
            while(true) {
               if (var15++ >= 2037) {
                  ZoneOffsetTransitionRule var34 = var5[var5.length - 2];
                  ZoneOffsetTransitionRule var35 = var5[var5.length - 1];
                  var9 = new int[10];
                  if (var34.offsetAfter - var34.offsetBefore < 0 && var35.offsetAfter - var35.offsetBefore > 0) {
                     ZoneOffsetTransitionRule var39 = var34;
                     var34 = var35;
                     var35 = var39;
                  }

                  var9[0] = var34.month - 1;
                  byte var42 = var34.dom;
                  var21 = var34.dow;
                  if (var21 == -1) {
                     var9[1] = var42;
                     var9[2] = 0;
                  } else if (var42 >= 0 && var42 < 24) {
                     var9[1] = var42;
                     var9[2] = -toCalendarDOW[var21];
                  } else {
                     var9[1] = -1;
                     var9[2] = toCalendarDOW[var21];
                  }

                  var9[3] = var34.secondOfDay * 1000;
                  var9[4] = toSTZTime[var34.timeDefinition];
                  var9[5] = var35.month - 1;
                  var42 = var35.dom;
                  var21 = var35.dow;
                  if (var21 == -1) {
                     var9[6] = var42;
                     var9[7] = 0;
                  } else if (var42 >= 0 && var42 < 24) {
                     var9[6] = var42;
                     var9[7] = -toCalendarDOW[var21];
                  } else {
                     var9[6] = -1;
                     var9[7] = toCalendarDOW[var21];
                  }

                  var9[8] = var35.secondOfDay * 1000;
                  var9[9] = toSTZTime[var35.timeDefinition];
                  var7 = (var34.offsetAfter - var34.offsetBefore) * 1000;
                  if (var9[2] == 6 && var9[3] == 0 && var0.equals("Asia/Amman")) {
                     var9[2] = 5;
                     var9[3] = 86400000;
                  }

                  if (var9[2] == 7 && var9[3] == 0 && var0.equals("Asia/Amman")) {
                     var9[2] = 6;
                     var9[3] = 86400000;
                  }

                  if (var9[7] == 6 && var9[8] == 0 && var0.equals("Africa/Cairo")) {
                     var9[7] = 5;
                     var9[8] = 86400000;
                  }
                  break;
               }

               ZoneOffsetTransitionRule[] var33 = var5;
               int var19 = var5.length;

               for(var36 = 0; var36 < var19; ++var36) {
                  ZoneOffsetTransitionRule var40 = var33[var36];
                  long var22 = var40.getTransitionEpochSecond(var15);
                  if (var13 + 2 >= var12.length) {
                     var12 = Arrays.copyOf(var12, var12.length + 100);
                  }

                  if (var14 + 1 >= var11.length) {
                     var11 = Arrays.copyOf(var11, var11.length + 100);
                  }

                  var13 = addTrans(var11, var14++, var12, var13, var22, var40.offsetAfter, var40.standardOffset);
               }
            }
         }

         if (var11 != null && var11.length != var14) {
            if (var14 == 0) {
               var11 = null;
            } else {
               var11 = Arrays.copyOf(var11, var14);
            }
         }

         if (var12 != null && var12.length != var13) {
            if (var13 == 0) {
               var12 = null;
            } else {
               var12 = Arrays.copyOf(var12, var13);
            }
         }

         if (var11 != null) {
            Checksum var41 = new Checksum();

            for(var16 = 0; var16 < var11.length; ++var16) {
               var38 = var11[var16];
               var21 = (int)(var38 >>> 4 & 15L);
               int var10000;
               if (var21 == 0) {
                  boolean var46 = false;
               } else {
                  var10000 = var12[var21];
               }

               int var44 = (int)(var38 & 15L);
               var10000 = var12[var44];
               long var45 = var38 >> 12;
               var41.update(var45 + (long)var44);
               var41.update(var44);
               var41.update(var21 == 0 ? -1 : var21);
            }

            var8 = (int)var41.getValue();
         }
      }

      return new ZoneInfo(var0, var32, var7, var8, var11, var12, var9, var10);
   }

   private static int getStandardOffset(long[] var0, int[] var1, long var2) {
      int var4;
      for(var4 = 0; var4 < var0.length && var2 >= var0[var4]; ++var4) {
      }

      return var1[var4];
   }

   private static int getYear(long var0, int var2) {
      long var3 = var0 + (long)var2;
      long var5 = Math.floorDiv(var3, 86400L);
      long var7 = var5 + 719528L;
      var7 -= 60L;
      long var9 = 0L;
      long var11;
      if (var7 < 0L) {
         var11 = (var7 + 1L) / 146097L - 1L;
         var9 = var11 * 400L;
         var7 += -var11 * 146097L;
      }

      var11 = (400L * var7 + 591L) / 146097L;
      long var13 = var7 - (365L * var11 + var11 / 4L - var11 / 100L + var11 / 400L);
      if (var13 < 0L) {
         --var11;
         var13 = var7 - (365L * var11 + var11 / 4L - var11 / 100L + var11 / 400L);
      }

      var11 += var9;
      int var15 = (int)var13;
      int var16 = (var15 * 5 + 2) / 153;
      int var17 = (var16 + 2) % 12 + 1;
      int var18 = var15 - (var16 * 306 + 5) / 10 + 1;
      var11 += (long)(var16 / 10);
      return (int)var11;
   }

   private static int indexOf(int[] var0, int var1, int var2, int var3) {
      for(var3 *= 1000; var1 < var2; ++var1) {
         if (var0[var1] == var3) {
            return var1;
         }
      }

      var0[var1] = var3;
      return var1;
   }

   private static int addTrans(long[] var0, int var1, int[] var2, int var3, long var4, int var6, int var7) {
      int var8 = indexOf(var2, 0, var3, var6);
      if (var8 == var3) {
         ++var3;
      }

      int var9 = 0;
      if (var6 != var7) {
         var9 = indexOf(var2, 1, var3, var6 - var7);
         if (var9 == var3) {
            ++var3;
         }
      }

      var0[var1] = var4 * 1000L << 12 | (long)(var9 << 4) & 240L | (long)var8 & 15L;
      return var3;
   }

   static {
      String var0 = ((String) AccessController.doPrivileged((PrivilegedAction)(new GetPropertyAction("sun.timezone.ids.oldmapping", "false")))).toLowerCase(Locale.ROOT);
      USE_OLDMAPPING = var0.equals("yes") || var0.equals("true");
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
         public Object run() {
            try {
               String var1 = System.getProperty("java.home") + File.separator + "lib";
               DataInputStream var2 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(var1, "tzdb.dat"))));
               Throwable var3 = null;

               try {
                  ZoneInfoFile.load(var2);
               } catch (Throwable var13) {
                  var3 = var13;
                  throw var13;
               } finally {
                  if (var2 != null) {
                     if (var3 != null) {
                        try {
                           var2.close();
                        } catch (Throwable var12) {
                           var3.addSuppressed(var12);
                        }
                     } else {
                        var2.close();
                     }
                  }

               }

               return null;
            } catch (Exception var15) {
               throw new Error(var15);
            }
         }
      });
      CURRT = System.currentTimeMillis() / 1000L;
      toCalendarDOW = new int[]{-1, 2, 3, 4, 5, 6, 7, 1};
      toSTZTime = new int[]{2, 0, 1};
   }

   private static class Checksum extends CRC32 {
      private Checksum() {
      }

      public void update(int var1) {
         byte[] var2 = new byte[]{(byte)(var1 >>> 24), (byte)(var1 >>> 16), (byte)(var1 >>> 8), (byte)var1};
         this.update(var2);
      }

      void update(long var1) {
         byte[] var3 = new byte[]{(byte)((int)(var1 >>> 56)), (byte)((int)(var1 >>> 48)), (byte)((int)(var1 >>> 40)), (byte)((int)(var1 >>> 32)), (byte)((int)(var1 >>> 24)), (byte)((int)(var1 >>> 16)), (byte)((int)(var1 >>> 8)), (byte)((int)var1)};
         this.update(var3);
      }

      // $FF: synthetic method
      Checksum(Object var1) {
         this();
      }
   }

   private static class ZoneOffsetTransitionRule {
      private final int month;
      private final byte dom;
      private final int dow;
      private final int secondOfDay;
      private final boolean timeEndOfDay;
      private final int timeDefinition;
      private final int standardOffset;
      private final int offsetBefore;
      private final int offsetAfter;

      ZoneOffsetTransitionRule(DataInput var1) throws IOException {
         int var2 = var1.readInt();
         int var3 = (var2 & 3670016) >>> 19;
         int var4 = (var2 & 507904) >>> 14;
         int var5 = (var2 & 4080) >>> 4;
         int var6 = (var2 & 12) >>> 2;
         int var7 = var2 & 3;
         this.month = var2 >>> 28;
         this.dom = (byte)(((var2 & 264241152) >>> 22) - 32);
         this.dow = var3 == 0 ? -1 : var3;
         this.secondOfDay = var4 == 31 ? var1.readInt() : var4 * 3600;
         this.timeEndOfDay = var4 == 24;
         this.timeDefinition = (var2 & 12288) >>> 12;
         this.standardOffset = var5 == 255 ? var1.readInt() : (var5 - 128) * 900;
         this.offsetBefore = var6 == 3 ? var1.readInt() : this.standardOffset + var6 * 1800;
         this.offsetAfter = var7 == 3 ? var1.readInt() : this.standardOffset + var7 * 1800;
      }

      long getTransitionEpochSecond(int var1) {
         long var2 = 0L;
         if (this.dom < 0) {
            var2 = toEpochDay(var1, this.month, lengthOfMonth(var1, this.month) + 1 + this.dom);
            if (this.dow != -1) {
               var2 = previousOrSame(var2, this.dow);
            }
         } else {
            var2 = toEpochDay(var1, this.month, this.dom);
            if (this.dow != -1) {
               var2 = nextOrSame(var2, this.dow);
            }
         }

         if (this.timeEndOfDay) {
            ++var2;
         }

         int var4 = 0;
         switch(this.timeDefinition) {
         case 0:
            var4 = 0;
            break;
         case 1:
            var4 = -this.offsetBefore;
            break;
         case 2:
            var4 = -this.standardOffset;
         }

         return var2 * 86400L + (long)this.secondOfDay + (long)var4;
      }

      static final boolean isLeapYear(int var0) {
         return (var0 & 3) == 0 && (var0 % 100 != 0 || var0 % 400 == 0);
      }

      static final int lengthOfMonth(int var0, int var1) {
         switch(var1) {
         case 2:
            return isLeapYear(var0) ? 29 : 28;
         case 3:
         case 5:
         case 7:
         case 8:
         case 10:
         default:
            return 31;
         case 4:
         case 6:
         case 9:
         case 11:
            return 30;
         }
      }

      static final long toEpochDay(int var0, int var1, int var2) {
         long var3 = (long)var0;
         long var5 = (long)var1;
         long var7 = 0L;
         var7 += 365L * var3;
         if (var3 >= 0L) {
            var7 += (var3 + 3L) / 4L - (var3 + 99L) / 100L + (var3 + 399L) / 400L;
         } else {
            var7 -= var3 / -4L - var3 / -100L + var3 / -400L;
         }

         var7 += (367L * var5 - 362L) / 12L;
         var7 += (long)(var2 - 1);
         if (var5 > 2L) {
            --var7;
            if (!isLeapYear(var0)) {
               --var7;
            }
         }

         return var7 - 719528L;
      }

      static final long previousOrSame(long var0, int var2) {
         return adjust(var0, var2, 1);
      }

      static final long nextOrSame(long var0, int var2) {
         return adjust(var0, var2, 0);
      }

      static final long adjust(long var0, int var2, int var3) {
         int var4 = (int) Math.floorMod(var0 + 3L, 7L) + 1;
         if (var3 < 2 && var4 == var2) {
            return var0;
         } else {
            int var5;
            if ((var3 & 1) == 0) {
               var5 = var4 - var2;
               return var0 + (long)(var5 >= 0 ? 7 - var5 : -var5);
            } else {
               var5 = var2 - var4;
               return var0 - (long)(var5 >= 0 ? 7 - var5 : -var5);
            }
         }
      }
   }
}
