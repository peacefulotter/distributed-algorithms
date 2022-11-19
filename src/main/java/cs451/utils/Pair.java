package cs451.utils;

import cs451.packet.Message;

import java.util.Objects;

public class Pair<A, B>
{
    private final A a;
    private final B b;

    public Pair(A a, B b)
    {
        this.a = a;
        this.b = b;
    }

    public static Pair<Integer, Integer> fromMessage( Message m )
    {
        return new Pair<>( m.getOrigin(), m.getSeqNr() );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals( a, pair.a ) && Objects.equals( b, pair.b );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( a, b );
    }

    @Override
    public String toString()
    {
        return "(" + "a=" + a + ", b=" + b + ')';
    }
}
