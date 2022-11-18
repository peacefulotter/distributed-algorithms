package cs451.network;

import cs451.Host;
import cs451.utils.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Timeout
{
    private static final String PREFIX = "Timeout";

    // private static final float MIN_DELTA_CHANGE = 10; // 10 ms
    private static final float DELTA_RATIO = 3; // 1/3 change
    private static final float INCREASE = 1.5f; //  *3/2
    private static final float DECREASE = 0.75f; // *3/4
    private static final int MIN = 25;
    private static final int BASE = 50;
    private static final int MAX = 1000;

    private static class Handler
    {
        public final AtomicInteger timeout = new AtomicInteger( BASE );
        public final AtomicLong lastIncrease = new AtomicLong( 0 );
        public final AtomicLong lastDecrease = new AtomicLong( 0 );

        /**
         * Prevent the timeout from changing if multiple increase or multiple decrease arrive
         * "at the same time" (i.e. with a time delta less than MIN_DELTA_CHANGE)
         */
        private boolean preventChange( AtomicLong last )
        {
            long current = System.nanoTime();
            boolean prevent = toMs( current - last.get() ) < (timeout.get() / DELTA_RATIO);
            last.set( System.nanoTime() );
            return prevent;
        }

        public boolean increase()
        {
            if ( preventChange( lastIncrease ) ) return false;
            timeout.set( (int) Math.min( timeout.get() * INCREASE, MAX ) );
            return true;
        }

        public boolean decrease()
        {
            if ( preventChange( lastDecrease ) ) return false;
            timeout.set( (int) Math.max( timeout.get() * DECREASE, MIN ) );
            return true;
        }
    }

    private final ConcurrentMap<Integer, Handler> handlers = new ConcurrentHashMap<>();

    public Timeout( List<Host> hosts )
    {
        for ( Host h : hosts )
            handlers.put( h.getId(), new Handler() );
    }

    public void increase( int id )
    {
        Handler h = handlers.get( id );
        int before = h.timeout.get();
        if ( h.increase() )
            Logger.log( PREFIX, "(" + id + ") Increasing " + before + " -> " + h.timeout.get() );
    }

    public void decrease( int id )
    {
        Handler h = handlers.get( id );
        int before = h.timeout.get();
        if ( h.decrease() )
            Logger.log( PREFIX, "(" + id + ") Decreasing " + before + " -> " + h.timeout.get() );
    }

    public int get( int id )
    {
        return handlers.get( id ).timeout.get();
    }

    public static long toMs( long ns )
    {
        return ns / 1000000;
    }
}
