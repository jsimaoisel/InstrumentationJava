package org.inescid.gsd.bdajvm.test;

import org.inescid.gsd.bdajvm.BDVMMethod;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class Main {
	private int a;

	@BDVMMethod
    public void operation() {
        System.out.println("I'm at MyClass::Operation");
    }

    public static void main(String[] args) {
        Main m1 = new Main();
        m1.operation();
        m1.operation();
        Main m2 = new Main();
        m2.operation();
        m2.operation();
    }
}


