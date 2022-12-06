
package cs451.utils;

import cs451.network.Timeout;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Stopwatch
{
    private static final DecimalFormat formatter = new DecimalFormat("###,###,###");
    private static final AtomicBoolean flag = new AtomicBoolean(false);
    private static final Map<Integer, Long> old_mem = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> start = new ConcurrentHashMap<>();

    private static Long getMem()
    {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static void init(int id)
    {
        start.put( id, System.nanoTime() );
        old_mem.put( id, getMem() );
        print(id);
    }

    private static void print(int id)
    {
        long delta = System.nanoTime() - start.get( id );
        long mem = getMem();
        Long old = old_mem.get( id );
        System.out.println(
            "\n================\n" +
            "Id: " + id + "\n" +
            formatter.format(mem - old) + " B\n" +
            Timeout.toMs(delta) + " ms\n" +
            "================\n"
        );
        old_mem.put( id, mem );
    }

    public static void stop(int id)
    {
//        if ( flag.get() ) return;
//        flag.set( true );
        print(id);
    }
}
