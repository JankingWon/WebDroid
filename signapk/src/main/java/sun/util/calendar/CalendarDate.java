package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

public abstract class CalendarDate implements Cloneable {
   public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;
   public static final long TIME_UNDEFINED = Long.MIN_VALUE;
   private Era era;
   private int year;
   private int month;
   private int dayOfMonth;
   private int dayOfWeek;
   private boolean leapYear;
   private int hours;
   private int minutes;
   private int seconds;
   private int millis;
   private long fraction;
   private boolean normalized;
   private TimeZone zoneinfo;
   private int zoneOffset;
   private int daylightSaving;
   private boolean forceStandardTime;
   private Locale locale;

   protected CalendarDate() {
      this(TimeZone.getDefault());
   }

   protected CalendarDate(TimeZone var1) {
      this.dayOfWeek = Integer.MIN_VALUE;
      this.zoneinfo = var1;
   }

   public Era getEra() {
      return this.era;
   }

   public CalendarDate setEra(Era var1) {
      if (this.era == var1) {
         return this;
      } else {
         this.era = var1;
         this.normalized = false;
         return this;
      }
   }

   public int getYear() {
      return this.year;
   }

   public CalendarDate setYear(int var1) {
      if (this.year != var1) {
         this.year = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addYear(int var1) {
      if (var1 != 0) {
         this.year += var1;
         this.normalized = false;
      }

      return this;
   }

   public boolean isLeapYear() {
      return this.leapYear;
   }

   void setLeapYear(boolean var1) {
      this.leapYear = var1;
   }

   public int getMonth() {
      return this.month;
   }

   public CalendarDate setMonth(int var1) {
      if (this.month != var1) {
         this.month = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addMonth(int var1) {
      if (var1 != 0) {
         this.month += var1;
         this.normalized = false;
      }

      return this;
   }

   public int getDayOfMonth() {
      return this.dayOfMonth;
   }

   public CalendarDate setDayOfMonth(int var1) {
      if (this.dayOfMonth != var1) {
         this.dayOfMonth = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addDayOfMonth(int var1) {
      if (var1 != 0) {
         this.dayOfMonth += var1;
         this.normalized = false;
      }

      return this;
   }

   public int getDayOfWeek() {
      if (!this.isNormalized()) {
         this.dayOfWeek = Integer.MIN_VALUE;
      }

      return this.dayOfWeek;
   }

   public int getHours() {
      return this.hours;
   }

   public CalendarDate setHours(int var1) {
      if (this.hours != var1) {
         this.hours = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addHours(int var1) {
      if (var1 != 0) {
         this.hours += var1;
         this.normalized = false;
      }

      return this;
   }

   public int getMinutes() {
      return this.minutes;
   }

   public CalendarDate setMinutes(int var1) {
      if (this.minutes != var1) {
         this.minutes = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addMinutes(int var1) {
      if (var1 != 0) {
         this.minutes += var1;
         this.normalized = false;
      }

      return this;
   }

   public int getSeconds() {
      return this.seconds;
   }

   public CalendarDate setSeconds(int var1) {
      if (this.seconds != var1) {
         this.seconds = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addSeconds(int var1) {
      if (var1 != 0) {
         this.seconds += var1;
         this.normalized = false;
      }

      return this;
   }

   public int getMillis() {
      return this.millis;
   }

   public CalendarDate setMillis(int var1) {
      if (this.millis != var1) {
         this.millis = var1;
         this.normalized = false;
      }

      return this;
   }

   public CalendarDate addMillis(int var1) {
      if (var1 != 0) {
         this.millis += var1;
         this.normalized = false;
      }

      return this;
   }

   public long getTimeOfDay() {
      return !this.isNormalized() ? (this.fraction = Long.MIN_VALUE) : this.fraction;
   }

   public CalendarDate setDate(int var1, int var2, int var3) {
      this.setYear(var1);
      this.setMonth(var2);
      this.setDayOfMonth(var3);
      return this;
   }

   public CalendarDate addDate(int var1, int var2, int var3) {
      this.addYear(var1);
      this.addMonth(var2);
      this.addDayOfMonth(var3);
      return this;
   }

   public CalendarDate setTimeOfDay(int var1, int var2, int var3, int var4) {
      this.setHours(var1);
      this.setMinutes(var2);
      this.setSeconds(var3);
      this.setMillis(var4);
      return this;
   }

   public CalendarDate addTimeOfDay(int var1, int var2, int var3, int var4) {
      this.addHours(var1);
      this.addMinutes(var2);
      this.addSeconds(var3);
      this.addMillis(var4);
      return this;
   }

   protected void setTimeOfDay(long var1) {
      this.fraction = var1;
   }

   public boolean isNormalized() {
      return this.normalized;
   }

   public boolean isStandardTime() {
      return this.forceStandardTime;
   }

   public void setStandardTime(boolean var1) {
      this.forceStandardTime = var1;
   }

   public boolean isDaylightTime() {
      if (this.isStandardTime()) {
         return false;
      } else {
         return this.daylightSaving != 0;
      }
   }

   protected void setLocale(Locale var1) {
      this.locale = var1;
   }

   public TimeZone getZone() {
      return this.zoneinfo;
   }

   public CalendarDate setZone(TimeZone var1) {
      this.zoneinfo = var1;
      return this;
   }

   public boolean isSameDate(CalendarDate var1) {
      return this.getDayOfWeek() == var1.getDayOfWeek() && this.getMonth() == var1.getMonth() && this.getYear() == var1.getYear() && this.getEra() == var1.getEra();
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof CalendarDate)) {
         return false;
      } else {
         CalendarDate var2 = (CalendarDate)var1;
         if (this.isNormalized() != var2.isNormalized()) {
            return false;
         } else {
            boolean var3 = this.zoneinfo != null;
            boolean var4 = var2.zoneinfo != null;
            if (var3 != var4) {
               return false;
            } else if (var3 && !this.zoneinfo.equals(var2.zoneinfo)) {
               return false;
            } else {
               return this.getEra() == var2.getEra() && this.year == var2.year && this.month == var2.month && this.dayOfMonth == var2.dayOfMonth && this.hours == var2.hours && this.minutes == var2.minutes && this.seconds == var2.seconds && this.millis == var2.millis && this.zoneOffset == var2.zoneOffset;
            }
         }
      }
   }

   public int hashCode() {
      long var1 = ((((long)this.year - 1970L) * 12L + (long)(this.month - 1)) * 30L + (long)this.dayOfMonth) * 24L;
      var1 = (((var1 + (long)this.hours) * 60L + (long)this.minutes) * 60L + (long)this.seconds) * 1000L + (long)this.millis;
      var1 -= (long)this.zoneOffset;
      int var3 = this.isNormalized() ? 1 : 0;
      int var4 = 0;
      Era var5 = this.getEra();
      if (var5 != null) {
         var4 = var5.hashCode();
      }

      int var6 = this.zoneinfo != null ? this.zoneinfo.hashCode() : 0;
      return (int)var1 * (int)(var1 >> 32) ^ var4 ^ var3 ^ var6;
   }

   public Object clone() {
      try {
         return super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new InternalError(var2);
      }
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      CalendarUtils.sprintf0d((StringBuilder)var1, this.year, 4).append('-');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.month, 2).append('-');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.dayOfMonth, 2).append('T');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.hours, 2).append(':');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.minutes, 2).append(':');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.seconds, 2).append('.');
      CalendarUtils.sprintf0d((StringBuilder)var1, this.millis, 3);
      if (this.zoneOffset == 0) {
         var1.append('Z');
      } else if (this.zoneOffset != Integer.MIN_VALUE) {
         int var2;
         char var3;
         if (this.zoneOffset > 0) {
            var2 = this.zoneOffset;
            var3 = '+';
         } else {
            var2 = -this.zoneOffset;
            var3 = '-';
         }

         var2 /= 60000;
         var1.append(var3);
         CalendarUtils.sprintf0d((StringBuilder)var1, var2 / 60, 2);
         CalendarUtils.sprintf0d((StringBuilder)var1, var2 % 60, 2);
      } else {
         var1.append(" local time");
      }

      return var1.toString();
   }

   protected void setDayOfWeek(int var1) {
      this.dayOfWeek = var1;
   }

   protected void setNormalized(boolean var1) {
      this.normalized = var1;
   }

   public int getZoneOffset() {
      return this.zoneOffset;
   }

   protected void setZoneOffset(int var1) {
      this.zoneOffset = var1;
   }

   public int getDaylightSaving() {
      return this.daylightSaving;
   }

   protected void setDaylightSaving(int var1) {
      this.daylightSaving = var1;
   }
}
