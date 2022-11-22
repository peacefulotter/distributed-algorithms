package cs451.lat;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class Proposal extends ConcurrentSkipListSet<Integer>
{
    public Proposal()
    {
    }

    public Proposal( Comparator<? super Integer> comparator )
    {
        super( comparator );
    }

    public Proposal( Collection<? extends Integer> c )
    {
        super( c );
    }

    public Proposal( SortedSet<Integer> s )
    {
        super( s );
    }
}
