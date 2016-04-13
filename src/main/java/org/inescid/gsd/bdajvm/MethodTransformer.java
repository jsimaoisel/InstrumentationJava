package org.inescid.gsd.bdajvm;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import sun.misc.Unsafe;

public class MethodTransformer implements ClassFileTransformer {

    private static String[] ignore = new String[] { "sun/", "java/", "javax/" };

    private Field theUnsafe;
    private Unsafe unsafe;


    public MethodTransformer() {
        try {
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public byte[] transform(ClassLoader l, String name, Class<?> c, ProtectionDomain d, byte[] b) throws IllegalClassFormatException {
        System.out.println("[Agent]: Running transformer for class " + name);
        for (int i = 0; i<ignore.length; i++) {
            if (name.startsWith(ignore[i])) {
                return b;
            }
        }
        System.out.println("[Agent]: Starting..." + name);

        CtClass cl = null;
        try {
            System.out.println("[Agent]: Reading class pool.");
            ClassPool pool = ClassPool.getDefault();
            System.out.println("[Agent]: Creating class in pool.");
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            System.out.println("[Agent]: Class loaded.");
            if (cl.isInterface() == false) {
				/*
				 * Check for annotated methods
				 */
                CtMethod[] methods = cl.getDeclaredMethods();
                System.out.println("[Agent]: Looking at methods");
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isEmpty() == false) {
                        // check if annotation is present in method
                        // insert prefix code
                        Object annotation = methods[i].getAnnotation(org.inescid.gsd.bdajvm.BDVMMethod.class);
                        if (annotation!=null) {
                            System.out.println("[Agent]: Before instrumenting " + methods[i].getName());
                            methods[i].insertBefore(
                                "{ java.lang.reflect.Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField(\"theUnsafe\");" +
                                "theUnsafe.setAccessible(true);"+
                                "sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);" +
                                "System.out.println(\"this klass ref = \" + unsafe.getInt($0, 8L)); }");
                            System.out.println("[Agent]: After instrumenting " + methods[i].getName());
                        }
                    }
                }
                b = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("Could not instrument  " + name + ",  exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return b;
    }
}
