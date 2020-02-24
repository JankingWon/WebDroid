package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

public final class Era {
   private final String name;
   private final String abbr;
   private final long since;
   private final CalendarDate sinceDate;
   private final boolean localTime;
   private int hash = 0;

   public Era(String var1, String var2, long var3, boolean var5) {
      this.name = var1;
      this.abbr = var2;
      this.since = var3;
      this.localTime = var5;
      Gregorian var6 = CalendarSystem.getGregorianCalendar();
      Gregorian.Date var7 = var6.newCalendarDate((TimeZone)null);
      var6.getCalendarDate(var3, (CalendarDate)var7);
      this.sinceDate = new ImmutableGregorianDate(var7);
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName(Locale var1) {
      return this.name;
   }

   public String getAbbreviation() {
      return this.abbr;
   }

   public String getDiaplayAbbreviation(Locale var1) {
      return this.abbr;
   }

   public long getSince(TimeZone var1) {
      if (var1 != null && this.localTime) {
         int var2 = var1.getOffset(this.since);
         return this.since - (long)var2;
      } else {
         return this.since;
      }
   }

   public CalendarDate getSinceDate() {
      return this.sinceDate;
   }

   public boolean isLocalTime() {
      return this.localTime;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof Era)) {
         return false;
      } else {
         Era var2 = (Era)var1;
         return this.name.equals(var2.name) && this.abbr.equals(var2.abbr) && this.since == var2.since && this.localTime == var2.localTime;
      }
   }

   public int hashCode() {
      if (this.hash == 0) {
         this.hash = this.name.hashCode() ^ this.abbr.hashCode() ^ (int)this.since ^ (int)(this.since >> 32) ^ (this.localTime ? 1 : 0);
      }

      return this.hash;
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      var1.append('[');
      var1.append(this.getName()).append(" (");
      var1.append(this.getAbbreviation()).append(')');
      var1.append(" since ").append((Object)this.getSinceDate());
      if (this.localTime) {
         var1.setLength(var1.length() - 1);
         var1.append(" local time");
      }

      var1.append(']');
      return var1.toString();
   }
}
