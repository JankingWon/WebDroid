package sun.util.calendar;

import java.util.TimeZone;

public abstract class BaseCalendar extends AbstractCalendar {
   public static final int JANUARY = 1;
   public static final int FEBRUARY = 2;
   public static final int MARCH = 3;
   public static final int APRIL = 4;
   public static final int MAY = 5;
   public static final int JUNE = 6;
   public static final int JULY = 7;
   public static final int AUGUST = 8;
   public static final int SEPTEMBER = 9;
   public static final int OCTOBER = 10;
   public static final int NOVEMBER = 11;
   public static final int DECEMBER = 12;
   public static final int SUNDAY = 1;
   public static final int MONDAY = 2;
   public static final int TUESDAY = 3;
   public static final int WEDNESDAY = 4;
   public static final int THURSDAY = 5;
   public static final int FRIDAY = 6;
   public static final int SATURDAY = 7;
   private static final int BASE_YEAR = 1970;
   private static final int[] FIXED_DATES = new int[]{719163, 719528, 719893, 720259, 720624, 720989, 721354, 721720, 722085, 722450, 722815, 723181, 723546, 723911, 724276, 724642, 725007, 725372, 725737, 726103, 726468, 726833, 727198, 727564, 727929, 728294, 728659, 729025, 729390, 729755, 730120, 730486, 730851, 731216, 731581, 731947, 732312, 732677, 733042, 733408, 733773, 734138, 734503, 734869, 735234, 735599, 735964, 736330, 736695, 737060, 737425, 737791, 738156, 738521, 738886, 739252, 739617, 739982, 740347, 740713, 741078, 741443, 741808, 742174, 742539, 742904, 743269, 743635, 744000, 744365};
   static final int[] DAYS_IN_MONTH = new int[]{31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
   static final int[] ACCUMULATED_DAYS_IN_MONTH = new int[]{-30, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
   static final int[] ACCUMULATED_DAYS_IN_MONTH_LEAP = new int[]{-30, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

   public boolean validate(CalendarDate var1) {
      Date var2 = (Date)var1;
      if (var2.isNormalized()) {
         return true;
      } else {
         int var3 = var2.getMonth();
         if (var3 >= 1 && var3 <= 12) {
            int var4 = var2.getDayOfMonth();
            if (var4 > 0 && var4 <= this.getMonthLength(var2.getNormalizedYear(), var3)) {
               int var5 = var2.getDayOfWeek();
               if (var5 != Integer.MIN_VALUE && var5 != this.getDayOfWeek(var2)) {
                  return false;
               } else if (!this.validateTime(var1)) {
                  return false;
               } else {
                  var2.setNormalized(true);
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public boolean normalize(CalendarDate var1) {
      if (var1.isNormalized()) {
         return true;
      } else {
         Date var2 = (Date)var1;
         TimeZone var3 = var2.getZone();
         if (var3 != null) {
            this.getTime(var1);
            return true;
         } else {
            int var4 = this.normalizeTime(var2);
            this.normalizeMonth(var2);
            long var5 = (long)var2.getDayOfMonth() + (long)var4;
            int var7 = var2.getMonth();
            int var8 = var2.getNormalizedYear();
            int var9 = this.getMonthLength(var8, var7);
            if (var5 > 0L && var5 <= (long)var9) {
               var2.setDayOfWeek(this.getDayOfWeek(var2));
            } else if (var5 <= 0L && var5 > -28L) {
               --var7;
               var9 = this.getMonthLength(var8, var7);
               var5 += (long)var9;
               var2.setDayOfMonth((int)var5);
               if (var7 == 0) {
                  var7 = 12;
                  var2.setNormalizedYear(var8 - 1);
               }

               var2.setMonth(var7);
            } else if (var5 > (long)var9 && var5 < (long)(var9 + 28)) {
               var5 -= (long)var9;
               ++var7;
               var2.setDayOfMonth((int)var5);
               if (var7 > 12) {
                  var2.setNormalizedYear(var8 + 1);
                  var7 = 1;
               }

               var2.setMonth(var7);
            } else {
               long var10 = var5 + this.getFixedDate(var8, var7, 1, var2) - 1L;
               this.getCalendarDateFromFixedDate(var2, var10);
            }

            var1.setLeapYear(this.isLeapYear(var2.getNormalizedYear()));
            var1.setZoneOffset(0);
            var1.setDaylightSaving(0);
            var2.setNormalized(true);
            return true;
         }
      }
   }

   void normalizeMonth(CalendarDate var1) {
      Date var2 = (Date)var1;
      int var3 = var2.getNormalizedYear();
      long var4 = (long)var2.getMonth();
      if (var4 <= 0L) {
         long var6 = 1L - var4;
         var3 -= (int)(var6 / 12L + 1L);
         var4 = 13L - var6 % 12L;
         var2.setNormalizedYear(var3);
         var2.setMonth((int)var4);
      } else if (var4 > 12L) {
         var3 += (int)((var4 - 1L) / 12L);
         var4 = (var4 - 1L) % 12L + 1L;
         var2.setNormalizedYear(var3);
         var2.setMonth((int)var4);
      }

   }

   public int getYearLength(CalendarDate var1) {
      return this.isLeapYear(((Date)var1).getNormalizedYear()) ? 366 : 365;
   }

   public int getYearLengthInMonths(CalendarDate var1) {
      return 12;
   }

   public int getMonthLength(CalendarDate var1) {
      Date var2 = (Date)var1;
      int var3 = var2.getMonth();
      if (var3 >= 1 && var3 <= 12) {
         return this.getMonthLength(var2.getNormalizedYear(), var3);
      } else {
         throw new IllegalArgumentException("Illegal month value: " + var3);
      }
   }

   private int getMonthLength(int var1, int var2) {
      int var3 = DAYS_IN_MONTH[var2];
      if (var2 == 2 && this.isLeapYear(var1)) {
         ++var3;
      }

      return var3;
   }

   public long getDayOfYear(CalendarDate var1) {
      return this.getDayOfYear(((Date)var1).getNormalizedYear(), var1.getMonth(), var1.getDayOfMonth());
   }

   final long getDayOfYear(int var1, int var2, int var3) {
      return (long)var3 + (long)(this.isLeapYear(var1) ? ACCUMULATED_DAYS_IN_MONTH_LEAP[var2] : ACCUMULATED_DAYS_IN_MONTH[var2]);
   }

   public long getFixedDate(CalendarDate var1) {
      if (!var1.isNormalized()) {
         this.normalizeMonth(var1);
      }

      return this.getFixedDate(((Date)var1).getNormalizedYear(), var1.getMonth(), var1.getDayOfMonth(), (Date)var1);
   }

   public long getFixedDate(int var1, int var2, int var3, Date var4) {
      boolean var5 = var2 == 1 && var3 == 1;
      if (var4 != null && var4.hit(var1)) {
         return var5 ? var4.getCachedJan1() : var4.getCachedJan1() + this.getDayOfYear(var1, var2, var3) - 1L;
      } else {
         int var6 = var1 - 1970;
         long var7;
         if (var6 >= 0 && var6 < FIXED_DATES.length) {
            var7 = (long)FIXED_DATES[var6];
            if (var4 != null) {
               var4.setCache(var1, var7, this.isLeapYear(var1) ? 366 : 365);
            }

            return var5 ? var7 : var7 + this.getDayOfYear(var1, var2, var3) - 1L;
         } else {
            var7 = (long)var1 - 1L;
            long var9 = (long)var3;
            if (var7 >= 0L) {
               var9 += 365L * var7 + var7 / 4L - var7 / 100L + var7 / 400L + (long)((367 * var2 - 362) / 12);
            } else {
               var9 += 365L * var7 + CalendarUtils.floorDivide(var7, 4L) - CalendarUtils.floorDivide(var7, 100L) + CalendarUtils.floorDivide(var7, 400L) + (long)CalendarUtils.floorDivide(367 * var2 - 362, 12);
            }

            if (var2 > 2) {
               var9 -= this.isLeapYear(var1) ? 1L : 2L;
            }

            if (var4 != null && var5) {
               var4.setCache(var1, var9, this.isLeapYear(var1) ? 366 : 365);
            }

            return var9;
         }
      }
   }

   public void getCalendarDateFromFixedDate(CalendarDate var1, long var2) {
      Date var4 = (Date)var1;
      int var5;
      long var6;
      boolean var8;
      if (var4.hit(var2)) {
         var5 = var4.getCachedYear();
         var6 = var4.getCachedJan1();
         var8 = this.isLeapYear(var5);
      } else {
         var5 = this.getGregorianYearFromFixedDate(var2);
         var6 = this.getFixedDate(var5, 1, 1, (Date)null);
         var8 = this.isLeapYear(var5);
         var4.setCache(var5, var6, var8 ? 366 : 365);
      }

      int var9 = (int)(var2 - var6);
      long var10 = var6 + 31L + 28L;
      if (var8) {
         ++var10;
      }

      if (var2 >= var10) {
         var9 += var8 ? 1 : 2;
      }

      int var12 = 12 * var9 + 373;
      if (var12 > 0) {
         var12 /= 367;
      } else {
         var12 = CalendarUtils.floorDivide(var12, 367);
      }

      long var13 = var6 + (long)ACCUMULATED_DAYS_IN_MONTH[var12];
      if (var8 && var12 >= 3) {
         ++var13;
      }

      int var15 = (int)(var2 - var13) + 1;
      int var16 = getDayOfWeekFromFixedDate(var2);

      assert var16 > 0 : "negative day of week " + var16;

      var4.setNormalizedYear(var5);
      var4.setMonth(var12);
      var4.setDayOfMonth(var15);
      var4.setDayOfWeek(var16);
      var4.setLeapYear(var8);
      var4.setNormalized(true);
   }

   public int getDayOfWeek(CalendarDate var1) {
      long var2 = this.getFixedDate(var1);
      return getDayOfWeekFromFixedDate(var2);
   }

   public static final int getDayOfWeekFromFixedDate(long var0) {
      return var0 >= 0L ? (int)(var0 % 7L) + 1 : (int)CalendarUtils.mod(var0, 7L) + 1;
   }

   public int getYearFromFixedDate(long var1) {
      return this.getGregorianYearFromFixedDate(var1);
   }

   final int getGregorianYearFromFixedDate(long var1) {
      long var3;
      int var5;
      int var6;
      int var7;
      int var8;
      int var9;
      int var10;
      int var11;
      int var12;
      if (var1 > 0L) {
         var3 = var1 - 1L;
         var9 = (int)(var3 / 146097L);
         var5 = (int)(var3 % 146097L);
         var10 = var5 / '躬';
         var6 = var5 % '躬';
         var11 = var6 / 1461;
         var7 = var6 % 1461;
         var12 = var7 / 365;
         var8 = var7 % 365 + 1;
      } else {
         var3 = var1 - 1L;
         var9 = (int)CalendarUtils.floorDivide(var3, 146097L);
         var5 = (int)CalendarUtils.mod(var3, 146097L);
         var10 = CalendarUtils.floorDivide(var5, 36524);
         var6 = CalendarUtils.mod(var5, 36524);
         var11 = CalendarUtils.floorDivide(var6, 1461);
         var7 = CalendarUtils.mod(var6, 1461);
         var12 = CalendarUtils.floorDivide(var7, 365);
         var8 = CalendarUtils.mod(var7, 365) + 1;
      }

      int var13 = 400 * var9 + 100 * var10 + 4 * var11 + var12;
      if (var10 != 4 && var12 != 4) {
         ++var13;
      }

      return var13;
   }

   protected boolean isLeapYear(CalendarDate var1) {
      return this.isLeapYear(((Date)var1).getNormalizedYear());
   }

   boolean isLeapYear(int var1) {
      return CalendarUtils.isGregorianLeapYear(var1);
   }

   public abstract static class Date extends CalendarDate {
      int cachedYear = 2004;
      long cachedFixedDateJan1 = 731581L;
      long cachedFixedDateNextJan1;

      protected Date() {
         this.cachedFixedDateNextJan1 = this.cachedFixedDateJan1 + 366L;
      }

      protected Date(TimeZone var1) {
         super(var1);
         this.cachedFixedDateNextJan1 = this.cachedFixedDateJan1 + 366L;
      }

      public Date setNormalizedDate(int var1, int var2, int var3) {
         this.setNormalizedYear(var1);
         this.setMonth(var2).setDayOfMonth(var3);
         return this;
      }

      public abstract int getNormalizedYear();

      public abstract void setNormalizedYear(int var1);

      protected final boolean hit(int var1) {
         return var1 == this.cachedYear;
      }

      protected final boolean hit(long var1) {
         return var1 >= this.cachedFixedDateJan1 && var1 < this.cachedFixedDateNextJan1;
      }

      protected int getCachedYear() {
         return this.cachedYear;
      }

      protected long getCachedJan1() {
         return this.cachedFixedDateJan1;
      }

      protected void setCache(int var1, long var2, int var4) {
         this.cachedYear = var1;
         this.cachedFixedDateJan1 = var2;
         this.cachedFixedDateNextJan1 = var2 + (long)var4;
      }
   }
}
