package cs451.network;

import cs451.utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class Timeout
{
    private static final float MIN_DELTA = 2; // 2 ms
    private static final float MULT_INCREASE = 2f;
    private static final float MULT_DECREASE = 0.75f;
    private static final int MIN = 50;
    private static final int MAX = 1000;

    private final AtomicInteger timeout = new AtomicInteger( MIN );
    private long last = 0;

    public boolean preventChange()
    {
        long current = System.nanoTime();
        boolean prevent = toMs( current - last ) < MIN_DELTA;
        last = System.nanoTime();
        return prevent;
    }

    public void increase()
    {
        if ( preventChange() ) return;
        timeout.set( (int) Math.min( timeout.get() * MULT_INCREASE, MAX ) );
        Logger.log("timeout", "Increasing to " + timeout);
    }

    public void decrease()
    {
        if ( preventChange() ) return;
        timeout.set( (int) Math.max( timeout.get() * MULT_DECREASE, MIN ) );
        Logger.log("timeout", "Decreasing to " + timeout);
    }

    private static long toMs(long ns)
    {
        return ns / 1000000;
    }

    public static long time()
    {
        return toMs( System.nanoTime() );
    }

    public int get() { return timeout.get(); }
}
