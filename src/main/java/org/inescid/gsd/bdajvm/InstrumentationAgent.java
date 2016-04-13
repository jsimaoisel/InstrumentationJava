package org.inescid.gsd.bdajvm;
import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent]: starting with options = " + agentArgs);
        if (agentArgs != null) {

        }
        inst.addTransformer(new MethodTransformer(), false);
    }
}