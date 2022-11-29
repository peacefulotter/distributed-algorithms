package cs451.lat;

import java.util.*;

public class Proposal extends HashSet<Integer>
{
    public Proposal()
    {
    }

    public Proposal( Collection<? extends Integer> c )
    {
        super( c );
    }

    public Proposal( int initialCapacity, float loadFactor )
    {
        super( initialCapacity, loadFactor );
    }

    public Proposal( int initialCapacity )
    {
        super( initialCapacity );
    }
}
