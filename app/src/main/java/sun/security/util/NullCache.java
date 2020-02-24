package sun.security.util;

class NullCache<K, V> extends Cache<K, V> {
   static final Cache<Object, Object> INSTANCE = new NullCache();

   public NullCache() {
   }

   public int size() {
      return 0;
   }

   public void clear() {
   }

   public void put(K var1, V var2) {
   }

   public V get(Object var1) {
      return null;
   }

   public void remove(Object var1) {
   }

   public void setCapacity(int var1) {
   }

   public void setTimeout(int var1) {
   }

   public void accept(CacheVisitor<K, V> var1) {
   }
}
