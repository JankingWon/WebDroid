package sun.util.logging;

import java.util.List;

public interface LoggingProxy {
   Object getLogger(String var1);

   Object getLevel(Object var1);

   void setLevel(Object var1, Object var2);

   boolean isLoggable(Object var1, Object var2);

   void log(Object var1, Object var2, String var3);

   void log(Object var1, Object var2, String var3, Throwable var4);

   void log(Object var1, Object var2, String var3, Object... var4);

   List<String> getLoggerNames();

   String getLoggerLevel(String var1);

   void setLoggerLevel(String var1, String var2);

   String getParentLoggerName(String var1);

   Object parseLevel(String var1);

   String getLevelName(Object var1);

   int getLevelValue(Object var1);

   String getProperty(String var1);
}
