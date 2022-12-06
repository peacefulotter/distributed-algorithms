
package cs451.utils;

import cs451.network.Timeout;

import java.util.concurrent.atomic.AtomicBoolean;

public class Stopwatch
{
    private static final AtomicBoolean flag = new AtomicBoolean(false);
    private static long start = 0;

    public static void init()
    {
        start = System.nanoTime();
        print();
    }

    private static void print()
    {
        long delta = System.nanoTime() - start;
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println( "\n================\n" + mem + " - " + Timeout.toMs(delta) + " ms\n================\n" );
    }

    public static void stop()
    {
        if ( flag.get() ) return;
        flag.set( true );
        print();
    }
}
