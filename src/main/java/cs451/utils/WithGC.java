package cs451.utils;

import java.util.AbstractCollection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WithGC<T>
{
    private static class ConcurrentQueue<T>
    {
        public final ConcurrentLinkedQueue<T> q;
        public final AtomicInteger size;

        public ConcurrentQueue()
        {
            this.q = new ConcurrentLinkedQueue<>();
            this.size = new AtomicInteger(0);
        }

        public void add(T t)
        {
            q.add( t );
            size.incrementAndGet();
        }

        public boolean contains(T t)
        {
            return q.contains( t );
        }

        public void clearAndAdd( T t ) {
            q.clear();
            q.add( t );
            size.set( 1 );
        }
    }

    private final ConcurrentQueue<T> q1, q2;
    private final AtomicBoolean swapped;
    private final int maxSize;

    public WithGC( int maxSize )
    {
        this.q1 = new ConcurrentQueue<>();
        this.q2 = new ConcurrentQueue<>();
        this.swapped = new AtomicBoolean(false);
        this.maxSize = maxSize;
    }

    public void add(T t)
    {
        boolean swap = swapped.get();
        ConcurrentQueue<T> first = swap ? q2 : q1;
        ConcurrentQueue<T> second = swap ? q1 : q2;

        if ( second.size.get() >= maxSize )
        {
            first.clearAndAdd(t);
            swapped.compareAndSet( swap, !swap );
        }
        else if ( first.size.get() >= maxSize )
        {
            second.add(t);
        }
        else
            first.add( t );
    }

    public boolean contains(T t)
    {
        return q1.contains( t ) || q2.contains( t );
    }

    @Override
    public String toString()
    {
        return q1.size.get() + " " + q2.size.get();
    }
}
