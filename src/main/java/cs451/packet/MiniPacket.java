package cs451.packet;

import java.util.Comparator;
import java.util.Objects;

public class MiniPacket implements Comparable<MiniPacket>
{
    private static final Comparator<MiniPacket> COMPARATOR = getComparator();
    public final int seq, src, dest;

    public MiniPacket( int seq, int src, int dest )
    {
        this.seq = seq;
        this.src = src;
        this.dest = dest;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        MiniPacket that = (MiniPacket) o;
        return seq == that.seq && src == that.src && dest == that.dest;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( seq, src, dest );
    }

    private static Comparator<MiniPacket> getComparator()
    {
        return Comparator
            .comparing( MiniPacket::getSeq )
            .thenComparing( MiniPacket::getSrc )
            .thenComparing( MiniPacket::getDest );
    }

    @Override
    public int compareTo( MiniPacket mp )
    {
        return COMPARATOR.compare( this, mp );
    }

    public int getSeq()
    {
        return seq;
    }

    public int getSrc()
    {
        return src;
    }

    public int getDest()
    {
        return dest;
    }

    @Override
    public String toString()
    {
        return "DST=" + dest + ", SEQ=" + seq + ", SRC=" + src;
    }
}
