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

    private static final float DELTA_RATIO = 3; // 1/3 change
    private static final float INCREASE = 1.75f;
    private static final float SLOW_INCREASE = 1.2f;
    private static final float DECREASE = 0.9f;
    private static final int MAX_TO = 2000; // 2s
    private static final int MIN = 25;
    private static final int BASE = 100;

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
            if (!prevent)
                last.set( System.nanoTime() );
            return prevent;
        }

        public boolean increase()
        {
            if ( preventChange( lastIncrease ) ) return false;
            int to = timeout.get();
            float factor = (to < MAX_TO) ? INCREASE : SLOW_INCREASE;
            timeout.set( (int) (to * factor) );
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
    private final int service_id;

    public Timeout( int id, List<Host> hosts )
    {
        this.service_id = id;
        for ( Host h : hosts )
            handlers.put( h.getId(), new Handler() );
    }

    public void increase( int id )
    {
        Handler h = handlers.get( id );
        int before = h.timeout.get();
        if ( h.increase() )
            Logger.print( service_id, PREFIX, "(" + id + ") Increasing " + before + " -> " + h.timeout.get() );
    }

    public void decrease( int id )
    {
        Handler h = handlers.get( id );
        int before = h.timeout.get();
        if ( h.decrease() )
            Logger.print( service_id, PREFIX, "(" + id + ") Decreasing " + before + " -> " + h.timeout.get() );
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
