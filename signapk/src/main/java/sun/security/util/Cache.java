package sun.security.util;

import java.util.Arrays;
import java.util.Map;

public abstract class Cache<K, V> {
   protected Cache() {
   }

   public abstract int size();

   public abstract void clear();

   public abstract void put(K var1, V var2);

   public abstract V get(Object var1);

   public abstract void remove(Object var1);

   public abstract void setCapacity(int var1);

   public abstract void setTimeout(int var1);

   public abstract void accept(CacheVisitor<K, V> var1);

   public static <K, V> Cache<K, V> newSoftMemoryCache(int var0) {
      return new MemoryCache(true, var0);
   }

   public static <K, V> Cache<K, V> newSoftMemoryCache(int var0, int var1) {
      return new MemoryCache(true, var0, var1);
   }

   public static <K, V> Cache<K, V> newHardMemoryCache(int var0) {
      return new MemoryCache(false, var0);
   }

   public static <K, V> Cache<K, V> newNullCache() {
      return new NullCache<>();
   }

   public static <K, V> Cache<K, V> newHardMemoryCache(int var0, int var1) {
      return new MemoryCache(false, var0, var1);
   }

   public interface CacheVisitor<K, V> {
      void visit(Map<K, V> var1);
   }

   public static class EqualByteArray {
      private final byte[] b;
      private volatile int hash;

      public EqualByteArray(byte[] var1) {
         this.b = var1;
      }

      public int hashCode() {
         int var1 = this.hash;
         if (var1 == 0) {
            var1 = this.b.length + 1;

            for(int var2 = 0; var2 < this.b.length; ++var2) {
               var1 += (this.b[var2] & 255) * 37;
            }

            this.hash = var1;
         }

         return var1;
      }

      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (!(var1 instanceof Cache.EqualByteArray)) {
            return false;
         } else {
            EqualByteArray var2 = (EqualByteArray)var1;
            return Arrays.equals(this.b, var2.b);
         }
      }
   }
}
