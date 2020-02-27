package sun.misc;

/*import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.security.AccessControlContext;
import java.util.Map;
import sun.nio.ch.Interruptible;
import sun.reflect.ConstantPool;
import sun.reflect.annotation.AnnotationType;*/

public interface JavaLangAccess {
   /*ConstantPool getConstantPool(Class<?> var1);

   boolean casAnnotationType(Class<?> var1, AnnotationType var2, AnnotationType var3);

   AnnotationType getAnnotationType(Class<?> var1);

   Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(Class<?> var1);

   byte[] getRawClassAnnotations(Class<?> var1);

   byte[] getRawClassTypeAnnotations(Class<?> var1);

   byte[] getRawExecutableTypeAnnotations(Executable var1);

   <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> var1);

   void blockedOn(Thread var1, Interruptible var2);

   void registerShutdownHook(int var1, boolean var2, Runnable var3);
*/   int getStackTraceDepth(Throwable var1);

   StackTraceElement getStackTraceElement(Throwable var1, int var2);
/*
   String newStringUnsafe(char[] var1);

   Thread newThreadWithAcc(Runnable var1, AccessControlContext var2);

   void invokeFinalize(Object var1) throws Throwable;*/
}
