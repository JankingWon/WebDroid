package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

class ImmutableGregorianDate extends BaseCalendar.Date {
   private final BaseCalendar.Date date;

   ImmutableGregorianDate(BaseCalendar.Date var1) {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         this.date = var1;
      }
   }

   public Era getEra() {
      return this.date.getEra();
   }

   public CalendarDate setEra(Era var1) {
      this.unsupported();
      return this;
   }

   public int getYear() {
      return this.date.getYear();
   }

   public CalendarDate setYear(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addYear(int var1) {
      this.unsupported();
      return this;
   }

   public boolean isLeapYear() {
      return this.date.isLeapYear();
   }

   void setLeapYear(boolean var1) {
      this.unsupported();
   }

   public int getMonth() {
      return this.date.getMonth();
   }

   public CalendarDate setMonth(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addMonth(int var1) {
      this.unsupported();
      return this;
   }

   public int getDayOfMonth() {
      return this.date.getDayOfMonth();
   }

   public CalendarDate setDayOfMonth(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addDayOfMonth(int var1) {
      this.unsupported();
      return this;
   }

   public int getDayOfWeek() {
      return this.date.getDayOfWeek();
   }

   public int getHours() {
      return this.date.getHours();
   }

   public CalendarDate setHours(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addHours(int var1) {
      this.unsupported();
      return this;
   }

   public int getMinutes() {
      return this.date.getMinutes();
   }

   public CalendarDate setMinutes(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addMinutes(int var1) {
      this.unsupported();
      return this;
   }

   public int getSeconds() {
      return this.date.getSeconds();
   }

   public CalendarDate setSeconds(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addSeconds(int var1) {
      this.unsupported();
      return this;
   }

   public int getMillis() {
      return this.date.getMillis();
   }

   public CalendarDate setMillis(int var1) {
      this.unsupported();
      return this;
   }

   public CalendarDate addMillis(int var1) {
      this.unsupported();
      return this;
   }

   public long getTimeOfDay() {
      return this.date.getTimeOfDay();
   }

   public CalendarDate setDate(int var1, int var2, int var3) {
      this.unsupported();
      return this;
   }

   public CalendarDate addDate(int var1, int var2, int var3) {
      this.unsupported();
      return this;
   }

   public CalendarDate setTimeOfDay(int var1, int var2, int var3, int var4) {
      this.unsupported();
      return this;
   }

   public CalendarDate addTimeOfDay(int var1, int var2, int var3, int var4) {
      this.unsupported();
      return this;
   }

   protected void setTimeOfDay(long var1) {
      this.unsupported();
   }

   public boolean isNormalized() {
      return this.date.isNormalized();
   }

   public boolean isStandardTime() {
      return this.date.isStandardTime();
   }

   public void setStandardTime(boolean var1) {
      this.unsupported();
   }

   public boolean isDaylightTime() {
      return this.date.isDaylightTime();
   }

   protected void setLocale(Locale var1) {
      this.unsupported();
   }

   public TimeZone getZone() {
      return this.date.getZone();
   }

   public CalendarDate setZone(TimeZone var1) {
      this.unsupported();
      return this;
   }

   public boolean isSameDate(CalendarDate var1) {
      return var1.isSameDate(var1);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return !(var1 instanceof ImmutableGregorianDate) ? false : this.date.equals(((ImmutableGregorianDate)var1).date);
      }
   }

   public int hashCode() {
      return this.date.hashCode();
   }

   public Object clone() {
      return super.clone();
   }

   public String toString() {
      return this.date.toString();
   }

   protected void setDayOfWeek(int var1) {
      this.unsupported();
   }

   protected void setNormalized(boolean var1) {
      this.unsupported();
   }

   public int getZoneOffset() {
      return this.date.getZoneOffset();
   }

   protected void setZoneOffset(int var1) {
      this.unsupported();
   }

   public int getDaylightSaving() {
      return this.date.getDaylightSaving();
   }

   protected void setDaylightSaving(int var1) {
      this.unsupported();
   }

   public int getNormalizedYear() {
      return this.date.getNormalizedYear();
   }

   public void setNormalizedYear(int var1) {
      this.unsupported();
   }

   private void unsupported() {
      throw new UnsupportedOperationException();
   }
}
