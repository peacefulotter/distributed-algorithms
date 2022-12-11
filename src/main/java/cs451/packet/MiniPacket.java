package cs451.packet;

import java.util.Comparator;

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

    public MiniPacket revert()
    {
        return new MiniPacket( seq, dest, src );
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
}
