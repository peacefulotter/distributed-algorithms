package cs451.perfectlink;

import java.util.concurrent.atomic.AtomicInteger;

public class Timeout
{
    private static final float MIN_DELTA = 2000000;
    private static final float MULT_INCREASE = 2f;
    private static final float MULT_DECREASE = 0.75f;
    private static final int MIN = 50;
    private static final int MAX = 1000;

    private final Server parent;
    private final AtomicInteger timeout = new AtomicInteger( MIN );
    private long last = 0;

    public Timeout( Server parent )
    {
        this.parent = parent;
    }

    public boolean preventChange()
    {
        long current = System.nanoTime();
        boolean prevent = ( current - last ) < MIN_DELTA;
        System.out.println(current - last + "  " + prevent);
        last = System.nanoTime();
        return prevent;
    }

    public void increase()
    {
        if ( preventChange() ) return;
        timeout.set( (int) Math.min( timeout.get() * MULT_INCREASE, MAX ) );
        parent.log("timeout", "Increasing to " + timeout);
    }

    public void decrease()
    {
        if ( preventChange() ) return;
        timeout.set( (int) Math.max( timeout.get() * MULT_DECREASE, MIN ) );
        parent.log("timeout", "Decreasing to " + timeout);
    }

    public int get() { return timeout.get(); }
}
