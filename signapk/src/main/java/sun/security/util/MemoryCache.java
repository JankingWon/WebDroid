package sun.security.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class MemoryCache<K, V> extends Cache<K, V> {
   private static final float LOAD_FACTOR = 0.75F;
   private static final boolean DEBUG = false;
   private final Map<K, CacheEntry<K, V>> cacheMap;
   private int maxSize;
   private long lifetime;
   private final ReferenceQueue<V> queue;

   public MemoryCache(boolean var1, int var2) {
      this(var1, var2, 0);
   }

   public MemoryCache(boolean var1, int var2, int var3) {
      this.maxSize = var2;
      this.lifetime = (long)(var3 * 1000);
      if (var1) {
         this.queue = new ReferenceQueue();
      } else {
         this.queue = null;
      }

      int var4 = (int)((float)var2 / 0.75F) + 1;
      this.cacheMap = new LinkedHashMap(var4, 0.75F, true);
   }

   private void emptyQueue() {
      if (this.queue != null) {
         int var1 = this.cacheMap.size();

         while(true) {
            CacheEntry var2 = (CacheEntry)this.queue.poll();
            if (var2 == null) {
               return;
            }

            K var3 = (K)var2.getKey();
            if (var3 != null) {
               CacheEntry var4 = (CacheEntry)this.cacheMap.remove(var3);
               if (var4 != null && var2 != var4) {
                  this.cacheMap.put(var3, var4);
               }
            }
         }
      }
   }

   private void expungeExpiredEntries() {
      this.emptyQueue();
      if (this.lifetime != 0L) {
         int var1 = 0;
         long var2 = System.currentTimeMillis();
         Iterator var4 = this.cacheMap.values().iterator();

         while(var4.hasNext()) {
            CacheEntry var5 = (CacheEntry)var4.next();
            if (!var5.isValid(var2)) {
               var4.remove();
               ++var1;
            }
         }

      }
   }

   public synchronized int size() {
      this.expungeExpiredEntries();
      return this.cacheMap.size();
   }

   public synchronized void clear() {
      if (this.queue != null) {
         Iterator var1 = this.cacheMap.values().iterator();

         while(var1.hasNext()) {
            CacheEntry var2 = (CacheEntry)var1.next();
            var2.invalidate();
         }

         while(true) {
            if (this.queue.poll() != null) {
               continue;
            }
         }
      }

      this.cacheMap.clear();
   }

   public synchronized void put(K var1, V var2) {
      this.emptyQueue();
      long var3 = this.lifetime == 0L ? 0L : System.currentTimeMillis() + this.lifetime;
      CacheEntry var5 = this.newEntry(var1, var2, var3, this.queue);
      CacheEntry var6 = (CacheEntry)this.cacheMap.put(var1, var5);
      if (var6 != null) {
         var6.invalidate();
      } else {
         if (this.maxSize > 0 && this.cacheMap.size() > this.maxSize) {
            this.expungeExpiredEntries();
            if (this.cacheMap.size() > this.maxSize) {
               Iterator var7 = this.cacheMap.values().iterator();
               CacheEntry var8 = (CacheEntry)var7.next();
               var7.remove();
               var8.invalidate();
            }
         }

      }
   }

   public synchronized V get(Object var1) {
      this.emptyQueue();
      CacheEntry var2 = (CacheEntry)this.cacheMap.get(var1);
      if (var2 == null) {
         return null;
      } else {
         long var3 = this.lifetime == 0L ? 0L : System.currentTimeMillis();
         if (!var2.isValid(var3)) {
            this.cacheMap.remove(var1);
            return null;
         } else {
            return (V)var2.getValue();
         }
      }
   }

   public synchronized void remove(Object var1) {
      this.emptyQueue();
      CacheEntry var2 = (CacheEntry)this.cacheMap.remove(var1);
      if (var2 != null) {
         var2.invalidate();
      }

   }

   public synchronized void setCapacity(int var1) {
      this.expungeExpiredEntries();
      if (var1 > 0 && this.cacheMap.size() > var1) {
         Iterator var2 = this.cacheMap.values().iterator();

         for(int var3 = this.cacheMap.size() - var1; var3 > 0; --var3) {
            CacheEntry var4 = (CacheEntry)var2.next();
            var2.remove();
            var4.invalidate();
         }
      }

      this.maxSize = var1 > 0 ? var1 : 0;
   }

   public synchronized void setTimeout(int var1) {
      this.emptyQueue();
      this.lifetime = var1 > 0 ? (long)var1 * 1000L : 0L;
   }

   public synchronized void accept(CacheVisitor<K, V> var1) {
      this.expungeExpiredEntries();
      Map var2 = this.getCachedEntries();
      var1.visit(var2);
   }

   private Map<K, V> getCachedEntries() {
      HashMap var1 = new HashMap(this.cacheMap.size());
      Iterator var2 = this.cacheMap.values().iterator();

      while(var2.hasNext()) {
         CacheEntry var3 = (CacheEntry)var2.next();
         var1.put(var3.getKey(), var3.getValue());
      }

      return var1;
   }

   protected CacheEntry<K, V> newEntry(K var1, V var2, long var3, ReferenceQueue<V> var5) {
      return (CacheEntry)(var5 != null ? new SoftCacheEntry(var1, var2, var3, var5) : new HardCacheEntry(var1, var2, var3));
   }

   private interface CacheEntry<K, V> {
      boolean isValid(long var1);

      void invalidate();

      K getKey();

      V getValue();
   }

   private static class HardCacheEntry<K, V> implements CacheEntry<K, V> {
      private K key;
      private V value;
      private long expirationTime;

      HardCacheEntry(K var1, V var2, long var3) {
         this.key = var1;
         this.value = var2;
         this.expirationTime = var3;
      }

      public K getKey() {
         return this.key;
      }

      public V getValue() {
         return this.value;
      }

      public boolean isValid(long var1) {
         boolean var3 = var1 <= this.expirationTime;
         if (!var3) {
            this.invalidate();
         }

         return var3;
      }

      public void invalidate() {
         this.key = null;
         this.value = null;
         this.expirationTime = -1L;
      }
   }

   private static class SoftCacheEntry<K, V> extends SoftReference<V> implements CacheEntry<K, V> {
      private K key;
      private long expirationTime;

      SoftCacheEntry(K var1, V var2, long var3, ReferenceQueue<V> var5) {
         super(var2, var5);
         this.key = var1;
         this.expirationTime = var3;
      }

      public K getKey() {
         return this.key;
      }

      public V getValue() {
         return this.get();
      }

      public boolean isValid(long var1) {
         boolean var3 = var1 <= this.expirationTime && this.get() != null;
         if (!var3) {
            this.invalidate();
         }

         return var3;
      }

      public void invalidate() {
         this.clear();
         this.key = null;
         this.expirationTime = -1L;
      }
   }
}
