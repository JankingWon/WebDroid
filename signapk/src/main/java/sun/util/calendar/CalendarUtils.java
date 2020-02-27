package sun.util.calendar;

public class CalendarUtils {
   public static final boolean isGregorianLeapYear(int var0) {
      return var0 % 4 == 0 && (var0 % 100 != 0 || var0 % 400 == 0);
   }

   public static final boolean isJulianLeapYear(int var0) {
      return var0 % 4 == 0;
   }

   public static final long floorDivide(long var0, long var2) {
      return var0 >= 0L ? var0 / var2 : (var0 + 1L) / var2 - 1L;
   }

   public static final int floorDivide(int var0, int var1) {
      return var0 >= 0 ? var0 / var1 : (var0 + 1) / var1 - 1;
   }

   public static final int floorDivide(int var0, int var1, int[] var2) {
      if (var0 >= 0) {
         var2[0] = var0 % var1;
         return var0 / var1;
      } else {
         int var3 = (var0 + 1) / var1 - 1;
         var2[0] = var0 - var3 * var1;
         return var3;
      }
   }

   public static final int floorDivide(long var0, int var2, int[] var3) {
      if (var0 >= 0L) {
         var3[0] = (int)(var0 % (long)var2);
         return (int)(var0 / (long)var2);
      } else {
         int var4 = (int)((var0 + 1L) / (long)var2 - 1L);
         var3[0] = (int)(var0 - (long)(var4 * var2));
         return var4;
      }
   }

   public static final long mod(long var0, long var2) {
      return var0 - var2 * floorDivide(var0, var2);
   }

   public static final int mod(int var0, int var1) {
      return var0 - var1 * floorDivide(var0, var1);
   }

   public static final int amod(int var0, int var1) {
      int var2 = mod(var0, var1);
      return var2 == 0 ? var1 : var2;
   }

   public static final long amod(long var0, long var2) {
      long var4 = mod(var0, var2);
      return var4 == 0L ? var2 : var4;
   }

   public static final StringBuilder sprintf0d(StringBuilder var0, int var1, int var2) {
      long var3 = (long)var1;
      if (var3 < 0L) {
         var0.append('-');
         var3 = -var3;
         --var2;
      }

      int var5 = 10;

      int var6;
      for(var6 = 2; var6 < var2; ++var6) {
         var5 *= 10;
      }

      for(var6 = 1; var6 < var2 && var3 < (long)var5; ++var6) {
         var0.append('0');
         var5 /= 10;
      }

      var0.append(var3);
      return var0;
   }

   public static final StringBuffer sprintf0d(StringBuffer var0, int var1, int var2) {
      long var3 = (long)var1;
      if (var3 < 0L) {
         var0.append('-');
         var3 = -var3;
         --var2;
      }

      int var5 = 10;

      int var6;
      for(var6 = 2; var6 < var2; ++var6) {
         var5 *= 10;
      }

      for(var6 = 1; var6 < var2 && var3 < (long)var5; ++var6) {
         var0.append('0');
         var5 /= 10;
      }

      var0.append(var3);
      return var0;
   }
}
