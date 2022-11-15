package cs451.utils;

import java.util.AbstractCollection;
import java.util.concurrent.atomic.AtomicInteger;

public class WithGC<T>
{
    private final AbstractCollection<T> collection;
    private final AtomicInteger min;

    public WithGC( AbstractCollection<T> collection )
    {
        this.collection = collection;
        this.min = new AtomicInteger(0);
    }

    public void add(T t)
    {
        collection.add( t );
    }

    public boolean contains(T t)
    {
        return collection.contains( t );
    }

    public boolean remove(T t)
    {
        return collection.remove( t );
    }

    public boolean remove( int i )
    {
        return collection.remove( i );
    }

    public void gc()
    {
        // implement comparator between atomic var and T
        // as an interface callback
    }
}
