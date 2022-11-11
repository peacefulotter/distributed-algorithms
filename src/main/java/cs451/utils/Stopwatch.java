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
    }

    public static void stop(int s)
    {
        if ( flag.get() ) return;
        flag.set( true );
        long delta = System.nanoTime() - start;
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println( "\n================\n" + mem + "\n" + s + " - " + Timeout.toMs(delta) + " ms\n================\n" );
    }
}
