

import handlers.ClassXInvocationHandler;
import impl.ClassXImpl;
import schema.ClassX;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PermGenRemovalFrom7Validator/Java8MetaspaceValidator
 * set java vm arguments to -XX:MaxMetaspaceSize=64m / 128m
 */
public class Java8MetaSpaceSim {

    private static Map<String, ClassX> classLeakingMap = new HashMap<String, ClassX>();
    private final static int NB_ITERATIONS_DEFAULT = 50000;

    public static void main(String[] args) {

        System.out.println("app starting...");

        Date start = new Date();
        System.out.println(start);

        Runtime gfg = Runtime.getRuntime();
        long memory1, memory2;
        System.out.println("Total memory in MB is: " + gfg.totalMemory()/(1024*1024));
        memory1 = gfg.freeMemory();
        System.out.println("Initial free memory in MB is: " + memory1/(1024*1024));

        int nbIterations = (args != null && args.length == 1) ? Integer.parseInt(args[0]) : NB_ITERATIONS_DEFAULT;

        try {

            for (int i = 0; i < nbIterations; i++) {

                String fictiousClassloaderJAR = "file:" + i + ".jar";

                URL[] fictiousClassloaderURL = new URL[]{new URL(fictiousClassloaderJAR)};

                // Create a new classloader instance
                URLClassLoader newClassLoader = new URLClassLoader(fictiousClassloaderURL);

                // Create a new Proxy instance
                ClassX t = (ClassX) Proxy.newProxyInstance(newClassLoader,
                        new Class<?>[]{ClassX.class},
                        new ClassXInvocationHandler(new ClassXImpl()));

                // Add the new Proxy instance to the leaking HashMap
                classLeakingMap.put(fictiousClassloaderJAR, t);
            }
        } catch (Throwable t) {
            memory2 = gfg.freeMemory();
            Date end = new Date();
            System.out.println(end);
            System.out.println("Crashed in "+(end.getTime() - start.getTime())/1000+ " seconds");
            System.out.println("Final free memory in MB is: " + memory2/(1024*1024) +"\nMax memory in MB is: "+gfg.maxMemory()/(1024*1024));
            System.out.println("ERROR: " + t);
        }

        System.out.println("Done!");
    }
}